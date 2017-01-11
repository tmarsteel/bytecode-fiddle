# Opcodes

This document lines out all opcodes available on the virtual processor.

## Arguments

The `Arguments` column lists the types of the arguments the opcode has. It is basically a comma
 separated list of either `lit` for literal value and `reg` for register index.
 
For example the `ldc` opcode takes two arguemnts: the first is a literal value, the second one is a
register index.

The arguments are referred to by the `Description` column with a $ sign and their index. E.g.
$0 for first argument, $1 for second and so forth.

## Opcodes

|Numeric Value|Name                   |Short Name|Arguments|Description|
|-------------|-----------------------|----------|---------|-----------|
|0x00         |LOAD_CONSTANT          |ldc       |lit, reg |Writes the literal $0 into the register $1.|
|0x01         |MOVE                   |mov       |reg, reg |Copies the value in register $0 to register $1.|
|0x02         |ADD                    |add       |-        |Adds the values
|0x05         |MUL                    |mul       |-        |Multiplies the value in #a1 by the value in #a2. Stores the result in #a1|
|0x0B         |OR                     |or        |-        |Calculates the bitwise OR of the values in #a1 and #a2. Stores the result in #a1|
|0x0C         |AND                    |and       |-        |Calculates the bitwise AND of the values in #a1 and #a2. Stores the result in #a1|
|0x0D         |XOR                    |xor       |-        |Calculates the bitwise XOR of the values in #a1 and #a2. Stores the result in #a1|
|0x0E         |INCREMENT              |inc       |reg      |Increments the value in register $0 by 1.|
|0x12         |DECREMENT              |dec       |reg      |Decrements the value in register $0 by 1.|
|0x08         |EQUALS                 |eq        |-        |Checks whether the values in #a1 and #a2 are eqaul. If so, writes 1 to #a1, otherwise writes 0 #a1.|
|0x09         |GREATER_THAN           |gt        |-        |Checks whether the value in #a1 is greater than the value in #a2. If so, writes 1 to #a1, otherwise writes 0 #a1.|
|0x0A         |GREATER_THAN_OR_EQUAL  |gte       |-        |Checks whether the value in #a1 is greater than or equal to the value in #a2. If so, writes 1 to #a1, otherwise writes 0 #a1.|
|0x13         |LESS_THAN              |lt        |-        |Checks whether the value in #a1 is less than the value in #a2. If so, writes 1 to #a1, otherwise writes 0 #a1.|
|0x14         |LESS_THAN_OR_EQUAL     |lte       |-        |Checks whether the value in #a1 is less than or equal to the value in #a2. If so, writes 1 to #a1, otherwise writes 0 #a1.|
|0x03         |STORE                  |sto       |reg, reg |Writes the value of register $0 to the memory cell with the address stored in register $1|
|0x04         |RECALL                 |rcl       |reg, reg |Reads the memory cell at the address stored in register $0 and writes the value to register $1|
|0x06         |JUMP                   |jmp       |lit      |Sets #ip to the literal value $0|
|0x07         |CONDITIONAL_JUMP       |cjmp      |lit      |If #a1 holds the value 1, sets #ip to the literal value $0|
|0x10         |VARJUMP                |vjmp      |reg      |Copies the value of register $0 to #ip|
|0x11         |CONDITIONAL_VARJUMP    |cvjmp     |reg      |If #a1 holds the value 1, copies the value of register $0 to #ip|
|0x0F         |TERMINATE              |term      |-        |Halts the processor.|

### Debugging Opcodes

|Numeric Value|Name                   |Short Name|Arguments|Description|
|-------------|-----------------------|----------|---------|-----------|
|0x15         |DEBUG_CORE_STATE       |debug_core_state|-  |Prints the values of all register so the STDOUT of the VM host.|
|0x16         |DEBUG_MEMORY_RANGE     |debug_memory_range|reg, reg|Prints the values of all memory cells from $0 to $1 to the STDOUT of the VM host.|
