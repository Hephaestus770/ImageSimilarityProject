package com.pdemo.main;


import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                Demo demo = new Demo();
                demo.setVisible(true);

            }
        });

    }
}