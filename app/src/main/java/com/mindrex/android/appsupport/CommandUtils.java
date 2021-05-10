package com.mindrex.android.appsupport;

import com.mindrex.util.FixedException;
import com.topjohnwu.superuser.Shell;

import java.util.Arrays;

// Notice: This file might have been changed from original.

@SuppressWarnings({"unused", "RedundantSuppression"})
public final class CommandUtils {
    public static final boolean hasRootAccess;

    static {
        hasRootAccess = Shell.rootAccess();
    }

    @Deprecated
    public synchronized static CommandFeedback execute(String cmd, String... args) {
        final Shell.Result a = (cmd.equals("su") ? Shell.su(args) : Shell.sh(args)).exec();
        return new CommandFeedback(Utils.joinStringLines(a.getOut()), Utils.joinStringLines(a.getErr()), args, a.getCode());
    }

    @Deprecated
    public synchronized static void executeWithError(String cmd, String... args) throws Exception {
        final CommandFeedback a = execute(cmd, args);
        if (a.errorMessage.length() > 0) {
            throw new ProcessRuntimeException(cmd, a.errorMessage, a.exitCode);
        }
    }

    @Deprecated
    public static final class CommandFeedback extends Thread {
        public final String errorMessage;
        public final String outputMessage;
        public final String[] args;
        public final int exitCode;

        private CommandFeedback(String a, String b, String[] c, int d) {
            this.errorMessage = a;
            this.outputMessage = b;
            this.args = c;
            this.exitCode = d;
        }

        @Override
        public final String toString() {
            return "CommandFeedback{" +
                    "errorMessage='" + errorMessage + '\'' +
                    ", outputMessage='" + outputMessage + '\'' +
                    ", args=" + Arrays.toString(args) +
                    ", exitCode=" + exitCode +
                    '}';
        }
    }

    @Deprecated
    @SuppressWarnings("unused")
    public static final class ProcessRuntimeException extends FixedException {
        private static final long serialVersionUID = 6824066647478431290L;

        private final String a;
        private final String b;
        private final int c;

        private ProcessRuntimeException(String a, String b, int c) {
            super(b, null);
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public final String getErrorMessage() {
            return b;
        }

        public final int getErrorCode() {
            return c;
        }

        @Override
        public final String getMessage() {
            return "Error while executing process " + a + ", error message: " + b + ", error code: " + c;
        }

        @Override
        public final String toString() {
            return getClassName() + ": " + getMessage();
        }
    }
}