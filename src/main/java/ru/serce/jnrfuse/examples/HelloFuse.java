package ru.serce.jnrfuse.examples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import jnr.ffi.Pointer;
import jnr.ffi.types.mode_t;
import jnr.ffi.types.off_t;
import jnr.ffi.types.size_t;
import ru.serce.jnrfuse.ErrorCodes;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.FuseStubFS;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseFileInfo;

public class HelloFuse extends FuseStubFS {

    public static final String HELLO_PATH = "/hello";
    public static final String HELLO_STR = "Hello World!";
    private static final String MOUNT_PATH = "/home/adi/zfs";

    @Override
    public int getattr(String path, FileStat stat) {
        /*int res = 0;
        if (Objects.equals(path, "/")) {
            stat.st_mode.set(FileStat.S_IFDIR | 0755);
            stat.st_nlink.set(2);
        } else if ("/hello".equals(path)) {
            log("Reading attrs for: " + path);
            stat.st_mode.set(FileStat.S_IFREG | 0777);
            stat.st_nlink.set(1);
            stat.st_size.set(HELLO_STR.getBytes().length);
        } else {
            res = -ErrorCodes.ENOENT();
        }
        return res;*/

        /*File f = new File(MOUNT_PATH + path);
        if (f.isFile()) {
            stat.st_mode.set(0777);
            stat.st_size.set(f.length());
            stat.st_nlink.set(1);
            stat.st_blocks.set(((f.length() + 511L) / 512L));
            return 0;
        }
        else if (f.isDirectory()) {
            stat.st_mode.set(FileStat.S_IFDIR | 0755);
            return 0;
        }
        return -ErrorCodes.ENOENT();*/

    }

    @Override
    public int readdir(String path, Pointer buf, FuseFillDir filter, @off_t long offset, FuseFileInfo fi) {
       /* if (!"/".equals(path)) {
            return -ErrorCodes.ENOENT();
        }*/
        log("Reading dir: " + path);
        filter.apply(buf, ".", null, 0);
        filter.apply(buf, "..", null, 0);

        /*File f = new File(MOUNT_PATH + path);
        if (f.isDirectory()) {
            File[] fList = f.listFiles();
            for (File file : fList) {
                filter.apply(buf, file.toString(), null, 0);
            }

        }*/
        //filter.apply(buf, HELLO_PATH.substring(1), null, 0);
        return 0;
    }

    @Override
    public int open(String path, FuseFileInfo fi) {
        /*if (!HELLO_PATH.equals(path)) {
            return -ErrorCodes.ENOENT();
        }*/
        log("Opening: " + path);
        return 0;
    }

    /*@Override
    public int opendir(final String path, final FuseFileInfo info) {
        log("Opening dir: " + path);
        return 0;
    }*/

    @Override
    public int read(String path, Pointer buf, @size_t long size, @off_t long offset, FuseFileInfo fi) {
        /*if (!HELLO_PATH.equals(path)) {
            return -ErrorCodes.ENOENT();
        }*/

        //byte[] bytes = HELLO_STR.getBytes();
        log("Reading from: " + path + " from offset: " + offset);
        try {
            Path p = Paths.get(MOUNT_PATH + path);
            log("Path p: " + p.toString());
            byte[] bytes = Files.readAllBytes(p);
            int length = bytes.length;
            log("byte length: " + length);
            if (offset < length) {
                if (offset + size > length) {
                    size = length - offset;
                }
                buf.put(0, bytes, 0, bytes.length);
            } else {
                size = 0;
            }
            return (int) size;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int write(String path, Pointer buf, @size_t long size, @off_t long offset, FuseFileInfo fi) {
        try {
            log("Writing to: " + path);
            byte[] b = new byte[(int) size];
            buf.get(offset, b, 0, (int) size);
            FileOutputStream output = new FileOutputStream(MOUNT_PATH + path, true);
            output.write(b);
            output.close();
            return (int) size;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public int create(String path, @mode_t long mode, FuseFileInfo fi) {
        File f = new File(MOUNT_PATH + path);
        try {
            log("Creating  new file: " + path);
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int mkdir(String path, @mode_t long mode) {
        File f = new File(MOUNT_PATH + path);
        log("Trying to create new dir: " + path);
        if (f.exists()) {
            f = null;
            return -ErrorCodes.EEXIST();
        } else {
            f.mkdir();
        }
        return 0;
    }

    public static void main(String[] args) {
        HelloFuse stub = new HelloFuse();
        try {
            stub.mount(Paths.get(MOUNT_PATH), true);
            //stub.mount(Paths.get("/zfs/mnt"), true);
        } finally {
            stub.umount();
        }
    }

    public static void log(Object... obj) {
        for (Object o : obj) {
            System.out.println(o);
        }
    }
}
