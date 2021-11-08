package top.lrpc.compress;

import top.lrpc.extension.SPI;

@SPI
public interface Compress {

    byte[] compress(byte[] bytes);

    byte[] deCompress(byte[] bytes);
}
