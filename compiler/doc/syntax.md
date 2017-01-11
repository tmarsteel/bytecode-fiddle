# Assembly Language Syntax

This document outlines the syntax of the assembly language.

* Source files are parsed on a per-line basis. Thus, one command cannot span multiple lines.
* Leading and trailing whitespace (`\r`, `\n`, `\t`, `\s`) of all lines is ignored.
* Empty lines are ignored

## Comments

Lines beginning with two forward slashes (`//`) are ignored.

## Instructions

Each line results in one instruction (opcode + arguments). Each line must be formatted like so:

```
<opcode shortname> <argument...>
```

### Arguments

Arguments can either be literal values (decimal, hexadecimal, binary) or register references (# followed by the name of
the register).

|Prefix  |Base  |Examples        |
|--------|------|----------------|
|        |10    |`65535`, `0`    |
|0x      |16    |`0xFF`, `0x223C`|
|0b      |2     |`0b0010010011`  |

### Examples

```
ldc 0 #a1
add
sto #a1 #m1
rcl 0xFFFE #a1
```

## Jumps

Safe jumps can be done using jump labels. Lines starting with a colon (`:`) are jump labels.
Jump labels can be referenced using the same syntax. Wherever a jump label appears it is treated
as if it was a literal value; the value of jump label is the address of the instruction following immediately.

For example:

```
// repeats "do stuff" 5 times
ldc 0 #a1
:start
// do stuff
inc #a1
ldc 5 #a2
lte
cjmp :start
```

```
:start
ldc 0 #m1
ldc :start #a1
// #a2 now holds the address of the instruction "ldc 0 #m1"
```

Labels can be referenced from anywhere in the file; order of declaration and use is irrelevant:

```
eq
cjmp :if_then
// else code
jmp :if_end
:if_then
// then code
:if_end
```