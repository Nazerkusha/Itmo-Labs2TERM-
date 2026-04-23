import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class GraphViewer {

    private static final Color[] CURVE_COLORS = {
        new Color(0xE63946), new Color(0x2A9D8F), new Color(0xE9A000),
        new Color(0x6A4C93), new Color(0x457B9D), new Color(0xF4A261),
    };
    private static final Color DATA_COLOR = new Color(0x1D1D1D);
    private static final Color GRID_COLOR = new Color(0xDDDDDD);
    private static final Color AXIS_COLOR = new Color(0x333333);
    private static final Color BG_COLOR   = Color.WHITE;

    private static final int W=900, H=560, PL=75, PR=30, PT=50, PB=60;

    private record Curve(String label, Color color, double[] xs, double[] ys) {}

    public static List<String> plot(String title, double[] xData, double[] yData,
                                    LeastSq.Result[] results, String outDir) {
        new File(outDir).mkdirs();
        List<String> saved = new ArrayList<>();

        List<Curve> all = new ArrayList<>();
        int ci = 0;
        for (LeastSq.Result r : results) {
            if (!r.valid()) continue;
            double[] xs = denseSample(xData, 300);
            double[] ys = evalResult(r, xs);
            all.add(new Curve(r.name() + "  " + r.formula(),
                              CURVE_COLORS[ci++ % CURVE_COLORS.length], xs, ys));
        }

        BufferedImage combined = render(title, xData, yData, all);
        String p0 = saveImg(combined, outDir, sanitize(title) + "_combined");
        saved.add(p0);
        showWin(title + " — все аппроксимации", combined);

        ci = 0;
        for (LeastSq.Result r : results) {
            if (!r.valid()) continue;
            double[] xs = denseSample(xData, 300);
            double[] ys = evalResult(r, xs);
            List<Curve> one = List.of(new Curve(r.formula(),
                              CURVE_COLORS[ci++ % CURVE_COLORS.length], xs, ys));
            BufferedImage img = render(r.name(), xData, yData, one);
            String path = saveImg(img, outDir, sanitize(title) + "_" + sanitize(r.name()));
            saved.add(path);
            showWin(r.name(), img);
        }
        return saved;
    }

    private static BufferedImage render(String title, double[] xData, double[] yData,
                                        List<Curve> curves) {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(BG_COLOR); g.fillRect(0,0,W,H);

        int pw = W-PL-PR, ph = H-PT-PB;

        double xMin=Double.MAX_VALUE, xMax=-Double.MAX_VALUE,
               yMin=Double.MAX_VALUE, yMax=-Double.MAX_VALUE;
        for (double v:xData){xMin=Math.min(xMin,v);xMax=Math.max(xMax,v);}
        for (double v:yData){if(Double.isFinite(v)){yMin=Math.min(yMin,v);yMax=Math.max(yMax,v);}}
        for (Curve c:curves) for (double v:c.ys())
            if (Double.isFinite(v)&&Math.abs(v)<1e6){yMin=Math.min(yMin,v);yMax=Math.max(yMax,v);}
        double xp=(xMax-xMin)*.05+1e-9, yp=(yMax-yMin)*.10+1e-9;
        xMin-=xp; xMax+=xp; yMin-=yp; yMax+=yp;

        final double X0=xMin,X1=xMax,Y0=yMin,Y1=yMax;
        java.util.function.DoubleUnaryOperator tx = v -> PL+(v-X0)/(X1-X0)*pw;
        java.util.function.DoubleUnaryOperator ty = v -> PT+ph-(v-Y0)/(Y1-Y0)*ph;

        g.setFont(new Font("SansSerif",Font.PLAIN,10));
        FontMetrics fm=g.getFontMetrics();
        for (int i=0;i<=8;i++){
            double xv=xMin+i*(xMax-xMin)/8;
            int px=(int)tx.applyAsDouble(xv);
            g.setColor(GRID_COLOR); g.setStroke(new BasicStroke(.8f));
            g.drawLine(px,PT,px,PT+ph);
            g.setColor(AXIS_COLOR);
            String lb=String.format("%.3g",xv);
            g.drawString(lb, px-fm.stringWidth(lb)/2, PT+ph+14);
        }
        for (int i=0;i<=6;i++){
            double yv=yMin+i*(yMax-yMin)/6;
            int py=(int)ty.applyAsDouble(yv);
            g.setColor(GRID_COLOR); g.setStroke(new BasicStroke(.8f));
            g.drawLine(PL,py,PL+pw,py);
            g.setColor(AXIS_COLOR);
            String lb=String.format("%.3g",yv);
            g.drawString(lb, PL-fm.stringWidth(lb)-5, py+fm.getAscent()/2-1);
        }

        g.setColor(AXIS_COLOR); g.setStroke(new BasicStroke(1.5f));
        g.drawRect(PL,PT,pw,ph);
        g.setStroke(new BasicStroke(1f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,0,new float[]{5,3},0));
        if (xMin<=0&&0<=xMax) g.drawLine((int)tx.applyAsDouble(0),PT,(int)tx.applyAsDouble(0),PT+ph);
        if (yMin<=0&&0<=yMax) g.drawLine(PL,(int)ty.applyAsDouble(0),PL+pw,(int)ty.applyAsDouble(0));

        g.setStroke(new BasicStroke(1f));
        g.setFont(new Font("SansSerif",Font.BOLD,12)); g.setColor(AXIS_COLOR);
        g.drawString("x", PL+pw+5, PT+ph+4);
        Graphics2D gr=(Graphics2D)g.create();
        gr.rotate(-Math.PI/2,13,PT+ (double) ph /2);
        gr.drawString("y",13,PT+ph/2); gr.dispose();

        for (Curve c:curves){
            g.setColor(c.color());
            g.setStroke(new BasicStroke(2.2f));
            Path2D path=new Path2D.Double(); boolean down=false;
            for (int i=0;i<c.xs().length;i++){
                double xv=c.xs()[i],yv=c.ys()[i];
                if (!Double.isFinite(yv)||Math.abs(yv)>1e6||xv<xMin||xv>xMax){down=false;continue;}
                int px=(int)Math.round(tx.applyAsDouble(xv));
                int py=(int)Math.round(ty.applyAsDouble(yv));
                if (!down){path.moveTo(px,py);down=true;}else path.lineTo(px,py);
            }
            g.draw(path);
        }

        int r=5;
        for (int i=0;i<xData.length;i++){
            if (!Double.isFinite(yData[i])) continue;
            int px=(int)Math.round(tx.applyAsDouble(xData[i]));
            int py=(int)Math.round(ty.applyAsDouble(yData[i]));
            g.setColor(BG_COLOR);    g.fillOval(px-r,py-r,2*r,2*r);
            g.setColor(DATA_COLOR);  g.setStroke(new BasicStroke(1.5f));
            g.drawOval(px-r,py-r,2*r,2*r);
            g.fillOval(px-r+2,py-r+2,2*r-4,2*r-4);
        }

        drawLegend(g, curves, PL, PT, pw);

        g.setFont(new Font("SansSerif",Font.BOLD,14));
        g.setColor(new Color(0x1F3864));
        FontMetrics tfm=g.getFontMetrics();
        String t=title.length()>70?title.substring(0,68)+"…":title;
        g.drawString(t, PL+(pw-tfm.stringWidth(t))/2, PT-14);

        g.dispose();
        return img;
    }

    private static void drawLegend(Graphics2D g, List<Curve> curves,
                                   int px, int py, int pw){
        g.setFont(new Font("SansSerif",Font.PLAIN,10));
        FontMetrics fm=g.getFontMetrics();
        int lh=17, rows=curves.size()+1, lw=290;
        int lx=px+pw-lw-8, ly=py+8;
        int lH=rows*lh+10;
        g.setColor(new Color(255,255,255,210));
        g.fillRoundRect(lx,ly,lw,lH,8,8);
        g.setColor(new Color(0xCCCCCC)); g.setStroke(new BasicStroke(.8f));
        g.drawRoundRect(lx,ly,lw,lH,8,8);
        int y0=ly+5+fm.getAscent();

        g.setColor(DATA_COLOR); g.setStroke(new BasicStroke(1.5f));
        int mx=lx+11, my=y0-fm.getAscent()/2;
        g.fillOval(mx-4,my-4,8,8);
        g.setColor(Color.DARK_GRAY);
        g.drawString("Исходные данные", lx+22, y0); y0+=lh;
        for (Curve c:curves){
            g.setColor(c.color()); g.setStroke(new BasicStroke(2.2f));
            g.drawLine(lx+6,y0-fm.getAscent()/2,lx+20,y0-fm.getAscent()/2);
            g.setColor(Color.DARK_GRAY);
            String lb=c.label();
            while(fm.stringWidth(lb)>lw-28&&lb.length()>8) lb=lb.substring(0,lb.length()-4)+"…";
            g.drawString(lb, lx+24, y0); y0+=lh;
        }
    }


    private static void showWin(String title, BufferedImage img){
        SwingUtilities.invokeLater(()->{
            JFrame f=new JFrame(title);
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.add(new JScrollPane(new JLabel(new ImageIcon(img))));
            f.pack(); f.setLocationRelativeTo(null); f.setVisible(true);
        });
    }

    private static String saveImg(BufferedImage img, String dir, String name){
        String path=dir+File.separator+name+".png";
        try {
            ImageIO.write(img,"PNG",new File(path));
        } catch(Exception e){
            System.out.println(Main.red("  Не сохранено: "+path));
        } return path;
    }

    private static double[] denseSample(double[] xData, int n){
        double mn=Double.MAX_VALUE,mx=-Double.MAX_VALUE;
        for(double v:xData){mn=Math.min(mn,v);mx=Math.max(mx,v);}
        double[] xs=new double[n];
        for(int i=0;i<n;i++) xs[i]=mn+i*(mx-mn)/(n-1);
        return xs;
    }

    private static double[] evalResult(LeastSq.Result r, double[] xs){
        int N=xs.length; double[] ys=new double[N]; double[] c=r.coeffs();
        switch(r.name()){
            case "Линейная"            ->{for(int i=0;i<N;i++)ys[i]=c[0]+c[1]*xs[i];}
            case "Полином 2-й степени" ->{for(int i=0;i<N;i++)ys[i]=c[0]+c[1]*xs[i]+c[2]*xs[i]*xs[i];}
            case "Полином 3-й степени" ->{for(int i=0;i<N;i++)ys[i]=c[0]+c[1]*xs[i]+c[2]*xs[i]*xs[i]+c[3]*xs[i]*xs[i]*xs[i];}
            case "Экспоненциальная"    ->{for(int i=0;i<N;i++)ys[i]=c[0]*Math.exp(c[1]*xs[i]);}
            case "Логарифмическая"     ->{for(int i=0;i<N;i++)ys[i]=xs[i]>0?c[0]+c[1]*Math.log(xs[i]):Double.NaN;}
            case "Степенная"           ->{for(int i=0;i<N;i++)ys[i]=xs[i]>0?c[0]*Math.pow(xs[i],c[1]):Double.NaN;}
            default->java.util.Arrays.fill(ys,Double.NaN);
        }
        return ys;
    }

    private static String sanitize(String s){
        String clean = s.replaceAll("[^a-zA-Z0-9_]","_").replaceAll("_+","_");
        if (clean.startsWith("_")) clean = clean.substring(1);
        return clean.substring(0, Math.min(clean.length(), 35));
    }
}
