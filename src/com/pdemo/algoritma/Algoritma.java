package com.pdemo.algoritma;

import com.pdemo.veri.CImage;
import com.pdemo.veri.Digest;
import com.pdemo.veri.Features;
import com.pdemo.veri.Projections;
public class Algoritma {

    private static final double SQRT_TWO = Math.sqrt(2);
    private static final int UCHAR_MAX = 255;
    private static final double[] THETA_180;
    private static final double[] TAN_THETA_180;

    static {
        THETA_180 = new double[180];
        TAN_THETA_180 = new double[180];
        for (int i = 0; i < 180; i++) {
            THETA_180[i] = i * Math.PI / 180;
            TAN_THETA_180[i] = Math.tan(THETA_180[i]);
        }
    }

    //Yatayda 0 ile 180 derece açıyla geçen çizgiler için görselin merkezinden geçen
    //N adet çizginin radon projeksiyonlarını bulma
    public boolean radon_projections(CImage gorsel, int N, Projections proj) {
        int gen = gorsel.genislik;
        int yuk = gorsel.yukseklik;
        int D = (gen > yuk) ? gen : yuk;
        int x_off = (gen >> 1) + (gen & 0x1);
        int y_off = (yuk >> 1) + (yuk & 0x1);
        proj.R = new CImage(N, D);
        proj.pixel_perline = new int[N];
        proj.boyut = N;

        for (int i = 0; i < proj.pixel_perline.length; i++) {
            proj.pixel_perline[i] = 0;
        }

        CImage radon_map = proj.R;
        int[] pixel_perline = proj.pixel_perline;

        for (int k = 0; k < N / 4 + 1; k++) {
            double alpha = TAN_THETA_180[k];
            for (int x = 0; x < D; x++) {
                double y = alpha * (x - x_off);
                int yd = (int) Math.floor(y + (y >= 0 ? 0.5 : -0.5));
                if ((yd + y_off >= 0) && (yd + y_off < yuk) && (x < gen)) {
                    radon_map.data[k + x * N] = gorsel.data[x
                            + ((yd + y_off) * gen)];
                    pixel_perline[k]++;
                }

                if ((yd + x_off >= 0) && (yd + x_off < gen) && (k != N / 4)
                        && (x < yuk)) {
                    radon_map.data[(N / 2 - k) + x * N] = gorsel.data[(yd + x_off)
                            + x * gen];
                    pixel_perline[N / 2 - k]++;
                }
            }
        }

        int j = 0;
        for (int k = 3 * N / 4; k < N; k++) {
            double alpha = TAN_THETA_180[k];
            for (int x = 0; x < D; x++) {
                double y = alpha * (x - x_off);
                int yd = (int) Math.floor(y + (y >= 0 ? 0.5 : -0.5));
                if ((yd + y_off >= 0) && (yd + y_off < yuk) && (x < gen)) {
                    radon_map.data[k + x * N] = gorsel.data[x
                            + ((yd + y_off) * gen)];
                    pixel_perline[k]++;
                }

                if ((y_off - yd >= 0) && (y_off - yd < gen)
                        && (2 * y_off - x >= 0) && (2 * y_off - x < yuk)
                        && (k != 3 * N / 4)) {
                    radon_map.data[(k - j) + x * N] = gorsel.data[(-yd + y_off)
                            + (-(x - y_off) + y_off) * gen];
                    pixel_perline[k - j]++;
                }

            }

            j = j + 2;

        }

        return true;

    }
    // Feature (özellik) vektörünü bir radon projeksiyon haritasından hesaplama
    public boolean feature_vector(Projections projs, Features fea) {
        CImage projection_map = projs.R;
        int[] nb_perline = projs.pixel_perline;
        int N = projs.boyut;
        int D = projection_map.yukseklik;
        fea.features = new double[N];

        for (int i = 0; i < fea.features.length; i++) {
            fea.features[i] = 0;
        }

        fea.boyut = N;

        double[] feat_v = fea.features;
        double sum = 0.0;
        double sum_sqd = 0.0;

        for (int k = 0; k < N; k++) {
            double line_sum = 0.0;
            double line_sum_sqd = 0.0;
            int nb_pixels = nb_perline[k];
            for (int i = 0; i < D; i++) {
                line_sum += projection_map.data[k + (i * projection_map.genislik)] & 0xFF;
                line_sum_sqd += (projection_map.data[k
                        + (i * projection_map.genislik)] & 0xFF)
                        * (projection_map.data[k + (i * projection_map.genislik)] & 0xFF);
            }
            feat_v[k] = (line_sum_sqd / nb_pixels) - (line_sum * line_sum)
                    / (nb_pixels * nb_pixels);
            sum += feat_v[k];
            sum_sqd += feat_v[k] * feat_v[k];
        }

        double mean = sum / N;
        double var = Math.sqrt((sum_sqd / N) - (sum * sum) / (N * N));

        for (int i = 0; i < N; i++) {
            feat_v[i] = (feat_v[i] - mean) / var;
        }

        return true;
    }
    // Verilen vektörün dct'sini (ayrık kosinüs dönüşümü)hesaplama
    public boolean dct(Features fea, Digest dig) {

        int N = fea.boyut;
        int nb_katsayı = 40;

        dig.katsayi = new int[nb_katsayı];
        dig.boyut = nb_katsayı;

        double[] R = fea.features;
        int[] D = dig.katsayi;

        double[] D_temp = new double[nb_katsayı];

        for (int i = 0; i < D_temp.length; i++) {
            D_temp[i] = 0;
        }

        double max = 0.0;
        double min = 0.0;

        for (int k = 0; k < nb_katsayı; k++) {
            double sum = 0.0;
            for (int n = 0; n < N; n++) {
                double temp = R[n]
                        * Math.cos((Math.PI * (2 * n + 1) * k) / (2 * N));
                sum += temp;
            }
            if (k == 0)
                D_temp[k] = sum / Math.sqrt((double) N);
            else
                D_temp[k] = sum * SQRT_TWO / Math.sqrt((double) N);
            if (D_temp[k] > max)
                max = D_temp[k];
            if (D_temp[k] < min)
                min = D_temp[k];
        }

        for (int i = 0; i < nb_katsayı; i++) {
            D[i] = (int) (UCHAR_MAX * (D_temp[i] - min) / (max - min));
        }

        return true;

    }

    // input görsel için görüntü özetini hesaplama
    public boolean image_digest(CImage img, Digest digest, int N) {
        img.blur();
        Projections projs = new Projections();
        radon_projections(img, N, projs);
        Features features = new Features();
        feature_vector(projs, features);
        dct(features, digest);
        return true;
    }

    // iki vektörün çapraz korelasyonu
    // Digest(özet) struct'lar aynıysa 1 döndürür
    public double crosscorr(Digest x, Digest y) {

        int N = y.boyut;

        int[] x_katsayi = x.katsayi;
        int[] y_katsayi = y.katsayi;

        double[] r = new double[N];
        double sumx = 0.0;
        double sumy = 0.0;
        for (int i = 0; i < N; i++) {
            sumx += x_katsayi[i];
            sumy += y_katsayi[i];
        }

        double meanx = sumx / N;
        double meany = sumy / N;
        double max = 0;

        for (int d = 0; d < N; d++) {
            double num = 0.0;
            double denx = 0.0;
            double deny = 0.0;
            for (int i = 0; i < N; i++) {
                num += (x_katsayi[i] - meanx)
                        * (y_katsayi[(N + i - d) % N] - meany);
                denx += Math.pow((x_katsayi[i] - meanx), 2);
                deny += Math.pow((y_katsayi[(N + i - d) % N] - meany), 2);
            }
            r[d] = num / Math.sqrt(denx * deny);
            if (r[d] > max)
                max = r[d];
        }

        return max;
    }
    // iki resmi karşılaştırma

    public double compare_images(CImage imA, CImage imB) {
        int N = 180;

        Digest digestA = new Digest();
        image_digest(imA, digestA, N);

        Digest digestB = new Digest();
        image_digest(imB, digestB, N);

        double pcc = crosscorr(digestA, digestB);

        return pcc;
    }
}
