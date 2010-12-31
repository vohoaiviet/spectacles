package com.stromberglabs.visual.utils;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ShowImage extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private BufferedImage mImage;
	
	public ShowImage(BufferedImage img){
		mImage = img;
	}
	
    protected void paintComponent(Graphics g) {
        // Center image in this component.
        g.drawImage(mImage,25,25,mImage.getWidth(),mImage.getHeight(),this);
    }
	
    public void display(){
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new JScrollPane(this));
        f.setSize(mImage.getWidth()+50,mImage.getHeight()+50);
        f.setLocation(0,0);
        f.setVisible(true);
    }
}
