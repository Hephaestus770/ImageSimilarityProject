package com.pdemo.veri;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class CImage {
    public byte[] data;
    public int genislik;
    public int yukseklik;
    public int boyut;

    private static final int BYTE_SIZE = 8;

    public CImage(String filepath) {
        File f = new File(filepath);
        try {
            BufferedImage gorsel = ImageIO.read(f);
            genislik = gorsel.getWidth();
            yukseklik = gorsel.getHeight();
            int pixSayisi = genislik * yukseklik;
            int bilesenSayisi = gorsel.getColorModel().getNumComponents();
            int maxPixel = 0;
            data = new byte[genislik * yukseklik];
            if (gorsel.getColorModel().getComponentSize(0) == BYTE_SIZE) {
                // Components are byte sized
                int bufferBoyutu = pixSayisi * bilesenSayisi;
                boyut = bufferBoyutu;
                byte[] tempData;
                try {
                    tempData = new byte[bufferBoyutu];
                } catch (OutOfMemoryError e) {
                    System.out
                            .println("Buffer ayrılmaya çalışılırken kapatıldı: "
                                    + bufferBoyutu
                                    + ". Heap boyutunu artırın!");
                    throw e;
                }
                gorsel.getRaster()
                        .getDataElements(0, 0, genislik, yukseklik, tempData);

                if (bilesenSayisi == 1) {
                    gorsel.getRaster()
                            .getDataElements(0, 0, genislik, yukseklik, data);
                } else if (gorsel.getType() == BufferedImage.TYPE_3BYTE_BGR) {
                    int j = 0;
                    for (int i = 0; i < bufferBoyutu; i += bilesenSayisi) {
                        float y0 = ((tempData[i + 2] & 0xFF) * 25
                                + (tempData[i + 1] & 0xFF) * 129 + (tempData[i] & 0xFF) * 66);
                        int y = Math.round(y0 / 256.0f) + 16;
                        if (y > 255)
                            y = 255;
                        if (y > maxPixel) {
                            maxPixel = y;
                        }
                        this.data[j] = (byte) y;
                        j++;
                    }

                } else {
                    throw new IllegalArgumentException(
                            "Bu tür görsellerle çalışamaz: "
                                    + gorsel.getType());
                }
            } else {
                throw new IllegalArgumentException(
                        "Byte olamyan görüntü buffer'larıyla çalışmaza");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CImage() {
        // TODO Auto-generated constructor stub
    }

    public CImage(int N, int D) {
        data = new byte[D * N];
        genislik = N;
        yukseklik = D;
        boyut = N * D;
    }

    public void blur() {
        double b1 = -1.23227429;
        double b2 = 0.379624963;
        double a0 = 0.121062264;
        double a1 = -0.0384676233;
        double a2 = 0.110714294;
        double a3 = -0.0459582582;
        double pKatsayı = 0.560531139;
        double nKatsayı = 0.439468890;

        // X yönü
        double[] Y = new double[genislik];

        for (int y = 0; y < yukseklik; y++) {
            int ptrX = 0;
            int ptrY = 0;
            double yb = 0;
            double yp = 0;
            double xp = 0;
            ptrX = y * genislik;
            xp = data[ptrX] & 0xFF;
            yb = pKatsayı * xp;
            yp = pKatsayı * xp;

            for (int m = 0; m < genislik; m++) {
                double xc = data[ptrX] & 0xFF;
                ptrX++;
                double yc = a0 * xc + a1 * xp - b1 * yp - b2 * yb;
                Y[ptrY] = yc;
                ptrY++;
                xp = xc;
                yb = yp;
                yp = yc;
            }

            double xn = 0;
            double xa = 0;
            double yn = 0;
            double ya = 0;

            xn = data[ptrX - 1] & 0xFF;
            xa = xn;
            yn = nKatsayı * xn;
            ya = yn;

            for (int n = genislik - 1; n >= 0; n--) {
                ptrX--;
                double xc = data[ptrX] & 0xFF;
                ;
                double yc = a2 * xn + a3 * xa - b1 * yn - b2 * ya;
                xa = xn;
                xn = xc;
                ya = yn;
                yn = yc;
                ptrY--;
                data[ptrX] = (byte) (Y[ptrY] + yc);
            }

        }

        // Y yönü

        Y = new double[yukseklik];

        for (int x = 0; x < genislik; x++) {

            int ptrX = 0;
            int ptrY = 0;
            double yb = 0;
            double yp = 0;
            double xp = 0;
            ptrX = ptrX + x;
            xp = data[ptrX] & 0xFF;
            yb = pKatsayı * xp;
            yp = pKatsayı * xp;

            for (int m = 0; m < yukseklik; m++) {
                double xc = data[ptrX] & 0xFF;
                ptrX = ptrX + genislik;
                double yc = a0 * xc + a1 * xp - b1 * yp - b2 * yb;
                Y[ptrY] = yc;
                ptrY++;
                xp = xc;
                yb = yp;
                yp = yc;
            }

            double xn = 0;
            double xa = 0;
            double yn = 0;
            double ya = 0;

            xn = data[ptrX - genislik] & 0xFF;
            xa = xn;
            yn = nKatsayı * xn;
            ya = yn;

            for (int n = yukseklik - 1; n >= 0; n--) {
                ptrX = ptrX - genislik;
                double xc = data[ptrX] & 0xFF;
                ;
                double yc = a2 * xn + a3 * xa - b1 * yn - b2 * ya;
                xa = xn;
                xn = xc;
                ya = yn;
                yn = yc;
                ptrY--;
                data[ptrX] = (byte) (Y[ptrY] + yc);
            }

        }

    }

    public boolean equals(CImage cimage) {
        if (this.genislik != cimage.genislik)
            return false;
        if (this.yukseklik != cimage.yukseklik)
            return false;

        for (int i = 0; i < cimage.boyut; i++) {
            if (this.data[i] != cimage.data[i]) {
                return false;
            }
        }
        return true;
    }

    public void save(String path) {
        BufferedImage temp = new BufferedImage(genislik, yukseklik,
                BufferedImage.TYPE_BYTE_GRAY);
        temp.getRaster().setDataElements(0, 0, genislik, yukseklik, data);
        try {
            ImageIO.write(temp, "jpg", new File(path));
        } catch (IOException e) {
        }
    }

    public void printData() {
        for (byte temp : data) {
            System.out.println("" + (temp & 0xFF));
        }
    }


}
