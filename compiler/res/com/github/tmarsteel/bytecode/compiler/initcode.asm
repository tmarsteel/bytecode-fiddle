// this code sets up the execution environment

// the last addresses of memory are reserved for these values:
// 65534 codesize (value of #m1 at startup)
// 65533 address of the current stackframe (0 means no stackframe initialized)

// store code size in 65534
ldc 65534 #a1
sto #m1 #a1

// initialize first stack frame directly after code
// and write location of that frame into the currentStackframeAddress
mov #m1 #a1
inc #a1
ldc 65533 #m1
sto #a1 #m1
// write previous frame addr (which is 0)
ldc 0 #m1
sto #m1 #a1
// write return jump addr (which is 0)
inc #a1
sto #m1 #a1
// write return value (which is 0)
inc #a1
sto #m1 #a1
// write number of parameters (which is 0)
inc #a1
sto #m1 #a1

// ready to go!