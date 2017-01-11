# Virtual Runtime

## Memory

The runtime provides 65536 memory cells each 64 bits in size. They are addressed from 0 to 65535.

At startup, all memory cells are initialized with 0. The code loaded into the runtime will be written
to the memory starting at address 0.

## Processor

The virtual processor has 11 registers of each 64 bits.

|Name               |Short Name|Description                 |
|-------------------|----------|----------------------------|
|Memory 1           |#m1       |simple read & write register|
|Memory 2           |#m2       |simple read & write register|
|Memory 3           |#m3       |simple read & write register|
|Memory 4           |#m4       |simple read & write register|
|Memory 5           |#m5       |simple read & write register|
|Memory 6           |#m6       |simple read & write register|
|Memory 7           |#m7       |simple read & write register|
|Memory 8           |#m8       |simple read & write register|
|Accumulator 1      |#a1       |Accumulator register. First operand of arithmetic opcodes. Results of arithmetic opcodes are written to this register.|
|Accumulator 2      |#a2       |Accumulator register. Second operand of arithmetic opcodes.|
|Instruction Pointer|#ip       |Holds the current instruction. Can be written to in order to jump.|

All registers are initialized with 0, except #m1. The size of the code loaded into the virtual machine
is written to #m1 at startup.

The processor executes this logic until it encounters a `term` opcode:
Read the instruction from memory address #ip and execute it.