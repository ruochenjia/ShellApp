#include "android.h"
#include <filesystem>

int main(int argc, char **argv) {
    bool err = false;
    if (argc == 0) {
        cout << "Invalid arguments, use -help for more information" << endl;
        err = true;
    }
    const char *a = argv[0];
    if (strcmp(a, "mkdir") == 0)
        for (int i = 1; i < sizeof(argv); i++) {
            const char *b = argv[i];
            try {
                if (!create_directory(b)) {
                    cout << "Failed to create directory " + string(b);
                    err = true;
                }
            } catch (fserr e) {
                cout << "Failed to create directory " + string(b) + ", " + e.what();
                err = true;
            }
        }
    else if (strcmp(a, "touch") == 0)
        for (int i = 1; i < sizeof(argv); i++) {
            const string b = string(argv[i]);
            const string c = "touch " + b;
            if (system(c.c_str())) {
                cout << "Failed to create " + b;
                err = true;
            }
        }
    else if (strcmp(a, "rm") == 0)
        for (int i = 1; i < sizeof(argv); i++) {
            const string b = string(argv[i]);
            const string c = "rm " + b;
            if (system(c.c_str())) {
                cout << "Failed to remove " + b;
                err = true;
            }
        }
    else if (strcmp(a, "rmdir") == 0)
        for (int i = 1; i < sizeof(argv); i++) {
            const string b = string(argv[i]);
            const string c = "rmdir " + b;
            if (system(c.c_str())) {
                cout << "Failed to remove " + b;
                err = true;
            }
        }
    else return -1;

    return err;
}