package com.pdemo.main;

import com.pdemo.algoritma.Algoritma;
import com.pdemo.veri.CImage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


public class Demo extends JFrame {
    private JPanel panel1;
    private JProgressBar PhashBar;
    private JButton hesaplaButton;
    private JButton yükleButton1;
    private JButton yükleButton2;
    private JLabel Image1;
    private JLabel Image2;

    File gorselYolu = new File("");
    File gorselYolu2 = new File("");
    public Demo(){
        add(panel1);
        setSize(400,400);
        setTitle("PhashDemo");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);



        yükleButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               JFileChooser gorselDosya = new JFileChooser();
               int res = gorselDosya.showSaveDialog(null);

               if(res == JFileChooser.APPROVE_OPTION){
                   gorselYolu = new File(gorselDosya.getSelectedFile().getAbsolutePath());
                   ImageIcon icon = new ImageIcon(gorselYolu.toString());
                   Image1.setIcon(icon);
               }

            }
        });
        yükleButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser gorselDosya2 = new JFileChooser();
                int res = gorselDosya2.showSaveDialog(null);

                if(res == JFileChooser.APPROVE_OPTION){
                    gorselYolu2 = new File(gorselDosya2.getSelectedFile().getAbsolutePath());
                    ImageIcon icon = new ImageIcon(gorselYolu2.toString());
                    Image2.setIcon(icon);
                }

            }
        });
        hesaplaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Algoritma phash = new Algoritma();
                CImage gorsel1=new CImage(gorselYolu.toString());
                CImage gorsel2=new CImage(gorselYolu2.toString());
                double d = phash.compare_images(gorsel1, gorsel2) * 100;
                int i = (int)d; // progress bar int dışında bir değer almadığı için int'e çeviriyoruz

                PhashBar.setStringPainted(true);
                PhashBar.setValue(i);

            }
        });
    }
}
