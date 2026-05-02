.data
.org 0x00
buf: .byte '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_'

.data
.org 0x100
promt_Greet: .byte 19, 'What is your name/n0'
prompt_W:    .word 'W'
prompt_h:    .word 'h'
prompt_a:    .word 'a'
prompt_t:    .word 't'
prompt_sp1:  .word ' '
prompt_i:    .word 'i'
prompt_s:    .word 's'
prompt_sp2:  .word ' '
prompt_y:    .word 'y'
prompt_o:    .word 'o'
prompt_u:    .word 'u'
prompt_r:    .word 'r'
prompt_sp3:  .word ' '
prompt_n:    .word 'n'
prompt_a2:   .word 'a'
prompt_m:    .word 'm'
prompt_e2:   .word 'e'
prompt_q:    .word '?'
prompt_nl:   .word 10
prompt_end:  .word 0

pfx_H:       .word 'H'
pfx_e:       .word 'e'
pfx_l1:      .word 'l'
pfx_l2:      .word 'l'
pfx_o:       .word 'o'
pfx_comma:   .word ','
pfx_sp:      .word ' '
pfx_end:     .word 0

one:         .word 1
four:        .word 4
max_name:    .word 23
newline_val: .word 10
mask_ff:     .word 0xFF


; IO port pointers
io_out_ptr:  .word 0x84
io_in_ptr:   .word 0x80

; Variables
src_ptr:     .word 0
dst_ptr:     .word 0
name_len:    .word 0
null_count:  .word 0
char_tmp:    .word 0



.text
_start:
    load_imm    prompt_W
    store_addr  src_ptr

print_prompt:
    load_addr   src_ptr
    load_acc                      
    beqz        write_hello_pfx
    store_ind   io_out_ptr        
    load_addr   src_ptr
    add         four              
    store_addr  src_ptr
    jmp         print_prompt


write_hello_pfx:
    load_imm    pfx_H
    store_addr  src_ptr
    load_imm    0x00
    store_addr  dst_ptr

copy_pfx:
    load_addr   src_ptr
    load_acc                     
    beqz        read_name
    store_ind   dst_ptr           
    load_addr   src_ptr
    add         four
    store_addr  src_ptr
    load_addr   dst_ptr
    add         one
    store_addr  dst_ptr
    jmp         copy_pfx


read_name:
    load_imm    0
    store_addr  name_len
    load_imm    0
    store_addr  null_count

read_loop:
    load_addr   name_len
    sub         max_name
    beqz        do_overflow
    bgt         do_overflow

    load_imm   0x80
    load_acc                   
    store_addr  char_tmp
    beqz        store_null    ; символ == \0
    sub         newline_val
    beqz        name_done

    load_addr   char_tmp
    load_acc
    store_ind   dst_ptr           
    load_addr   dst_ptr
    add         one
    store_addr  dst_ptr
    load_addr   name_len
    add         one
    store_addr  name_len
    jmp         read_loop

name_done:
    load_addr   name_len
    beqz        do_overflow

    load_imm    '!'
    store_ind   dst_ptr
    load_addr   dst_ptr
    add         one
    store_addr  dst_ptr

    load_imm    0x5F5F5F00
    store_ind   dst_ptr


print_greet:
    load_imm    0x00
    store_addr  src_ptr

print_greet_loop:
    load_addr   src_ptr
    load_acc                      
    and         mask_ff           
    beqz        check_null
    store_ind   io_out_ptr        
    load_addr   src_ptr
    add         one
    store_addr  src_ptr
    jmp         print_greet_loop

check_null:
    load_addr   null_count
    load_acc
    beqz        done

    sub         one
    store_addr  null_count

    load_addr   src_ptr
    load_acc                      
    and         mask_ff
    store_ind   io_out_ptr
    load_addr   src_ptr
    add         one
    store_addr  src_ptr
    jmp         print_greet_loop

done:
    halt

do_overflow:
    load_imm    0xCCCCCCCC
    store_ind   io_out_ptr
    halt

store_null:
    load_imm    0
    store_ind   dst_ptr           ; записываем \0 в buf
    load_addr   dst_ptr
    add         one
    store_addr  dst_ptr
    load_addr   null_count
    add         one
    store_addr  null_count
    load_addr   name_len
    add         one
    store_addr  name_len
    jmp         read_loop