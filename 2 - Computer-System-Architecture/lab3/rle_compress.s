.data
    ; A1 - откуда читаем input (порт 0x80)
    ; A2 - куда пишем output  (порт 0x84)
    ; A3 - пишем сжатый результат сюда (начало 0x000)
    ; A4 - отсюда потом читаем и печатаем (тоже 0x000)
    ; A5 - сюда временно складываем входную строку (начало 0x200)
    ; D0 - текущий прочитанный символ
    ; D1 - символ текущего знака, который считаем
    ; D2 - сколько раз подряд встретился D1
    ; D3 - сколько байт уже написали в результат
    ; D4 - временная переменная для вычислений
    ; D5 - сколько байт прочитали из входа
.org 0x88
    .text
_start:
    movea.l  0x70, A7
    movea.l  0x80, A1
    movea.l  0x84, A2
    movea.l  0x000, A3
    movea.l  0x000, A4
    movea.l  0x1B8, A5
    move.l   0, D5
read_input:
    cmp.l    0x40, D5
    bge      overflow_instant
    move.b   (A1), D0
    cmp.b    0xA, D0
    beq      phase2
    move.b   D0, (A5)+
    add.l    1, D5
    jmp      read_input
phase2:
    movea.l  0x1B8, A5
    move.l   0, D3
    cmp.l    0, D5
    beq      end
    move.b   (A5)+, D1
    move.l   1, D2
    sub.l    1, D5
compress_loop:
    cmp.l    0, D5
    beq      flush_last
    move.b   (A5)+, D0
    sub.l    1, D5
    cmp.b    D1, D0
    bne      flush_and_new
    cmp.l    9, D2
    beq      flush_and_same
    add.l    1, D2
    jmp      compress_loop
flush_and_same:
    jsr      write_run
    move.l   1, D2
    jmp      compress_loop
flush_and_new:
    jsr      write_run
    move.b   D0, D1
    move.l   1, D2
    jmp      compress_loop
flush_last:
    jsr      write_run
    jmp      end
write_run:
    move.l   D3, D4
    add.l    2, D4
    cmp.l    0x40, D4
    bge      overflow_instant
    move.l   D2, D4
    add.l    0x30, D4
    move.b   D4, (A3)+
    move.b   D1, (A3)+
    add.l    2, D3
    rts
end:
    move.b   0, (A3)
print_loop:
    move.b   (A4)+, D0
    beq      hlt
    move.b   D0, (A2)
    jmp      print_loop
hlt:
    halt
overflow_instant:
    move.l   -858993460, D0
    move.l   D0, (A2)
    halt