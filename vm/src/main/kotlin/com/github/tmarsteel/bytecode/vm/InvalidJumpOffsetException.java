package com.github.tmarsteel.bytecode.vm;

import org.jetbrains.annotations.NotNull;

public final class InvalidJumpOffsetException extends VMRuntimeException
{
    @NotNull
    public final Long faultyOffset;

    public final Long faultyInstructionOffset;

    public InvalidJumpOffsetException(Long faultyOffset)
    {
        super("Invalid jump offset " + faultyOffset + "; cannot jump");

        this.faultyOffset = faultyOffset;
        this.faultyInstructionOffset = null;
    }

    public InvalidJumpOffsetException(Long faultyOffset, Long faultyInstructionOffset)
    {
        super("Invalid jump offset " + faultyOffset + "; cannot jump. Caused by instruction at offset " + faultyInstructionOffset);

        this.faultyOffset = faultyOffset;
        this.faultyInstructionOffset = faultyInstructionOffset;
    }
}
