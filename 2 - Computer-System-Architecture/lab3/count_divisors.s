.data
input_address:   .word 0x80
output_address:  .word 0x84

.text
.org 0x88
isDiv:
    rem     t0, a1, a2
    beqz    t0, isDivYes
    mv      a0, zero
    jr      ra
isDivYes:
    addi    a0, zero, 1
    jr      ra
cdRecr:
    bgt     a2, a1, cdBase
    addi    sp, sp, -12
    sw      ra, 8(sp)
    sw      a1, 4(sp)
    sw      a2, 0(sp)
    jal     ra, isDiv
    mv      t0, a0
    lw      a1, 4(sp)
    lw      a2, 0(sp)
    addi    a2, a2, 1
    lw      ra, 8(sp)
    sw      ra, 8(sp)
    lw      ra, 8(sp)
    addi    sp, sp, 12
    addi    sp, sp, -8
    sw      ra, 4(sp)
    sw      t0, 0(sp) 
    jal     ra, cdRecr
    lw      t0, 0(sp)
    lw      ra, 4(sp)
    addi    sp, sp, 8
    add     a0, a0, t0
    jr      ra
cdBase:
    mv      a0, zero
    jr      ra
cd:
    mv      t0, zero
    ble     a0, t0, cdErr
    addi    sp, sp, -8
    sw      ra, 4(sp)
    sw      a0, 0(sp)
    mv      a1, a0
    addi    a2, zero, 1
    jal     ra, cdRecr
    lw      ra, 4(sp)
    addi    sp, sp, 8
    jr      ra
cdErr:
    addi    a0, zero, -1
    jr      ra


_start:
    lui     sp, %hi(0x1000)
    addi    sp, sp, %lo(0x1000)
    lui     t0, %hi(input_address)
    addi    t0, t0, %lo(input_address)
    lw      t0, 0(t0)
    lw      a0, 0(t0)
    jal     ra, cd
    lui     t0, %hi(output_address)
    addi    t0, t0, %lo(output_address)
    lw      t0, 0(t0)
    sw      a0, 0(t0)

    halt