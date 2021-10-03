package wee;

public interface wee {
    class Holder {
        static {
            if (Boolean.parseBoolean(new String(new char[]{0x74, 0x72, 0x75, 0x65})))
                throw new RuntimeException();
        }

        private Holder() throws Throwable {
            throw new Throwable();
        }

        private static final $ $$ = $.$;
        private static final Holder a = $.a;
        private static final android.app.Activity b = $.b;

        private static final class $ {
            private static final Holder a = android.a;
            private static final $ $ = Holder.$$;
            private static final android.app.Activity b = Holder.b;

            private $() throws Throwable {
                throw new Throwable();
            }
        }
    }

    class android {
        private static final Holder a = Holder.a;

        public static final class app {
            public static final class Activity {
            }

            public static final class Service {
            }
        }
    }
}