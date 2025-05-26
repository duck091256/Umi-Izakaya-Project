package view;

import javax.swing.*;

import fx.RoundedLabelEffect;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/* class này vô nghĩa nha ae */

public class TaskBar extends JPanel {

    public TaskBar() {
        setBackground(new Color(45, 61, 77));
        setPreferredSize(new Dimension(450, 42));
        setLayout(null);

        RoundedLabelEffect rlbl_table = new RoundedLabelEffect("Phòng bàn");
        rlbl_table.setBounds(10, 11, 100, 31);
        rlbl_table.setBackground(new Color(45, 61, 75));
        rlbl_table.setForeground(new Color(255, 255, 255));
        rlbl_table.setHorizontalAlignment(SwingConstants.CENTER);
        rlbl_table.setFont(new Font("Arial", Font.PLAIN, 14));
        rlbl_table.setCornerRadius(10);
        
        
        rlbl_table.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseEntered(MouseEvent e) {
        		rlbl_table.setBackground(new Color(255, 255, 255));
        		rlbl_table.setForeground(new Color(166, 166, 166));
        		rlbl_table.setFont(new Font("Arial", Font.BOLD, 14));
        	}

	        @Override
	        public void mouseExited(MouseEvent e) {

	            rlbl_table.setBackground(new Color(45, 61, 75));
	            rlbl_table.setForeground(new Color(255, 255, 255));
	            rlbl_table.setFont(new Font("Arial", Font.PLAIN, 14));
	        }
        });
        
        
        add(rlbl_table);
        
        RoundedLabelEffect rlbl_menu = new RoundedLabelEffect("Thực đơn");
        rlbl_menu.setBounds(114, 11, 100, 31);
        rlbl_menu.setBackground(new Color(255, 255, 255));
        rlbl_menu.setForeground(new Color(166, 166, 166));
        rlbl_menu.setHorizontalAlignment(SwingConstants.CENTER);
        rlbl_menu.setFont(new Font("Arial", Font.BOLD, 14));
        rlbl_menu.setCornerRadius(10);
        
        rlbl_menu.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseEntered(MouseEvent e) {
        		rlbl_menu.setBackground(new Color(255, 255, 255));
        		rlbl_menu.setForeground(new Color(166, 166, 166));
        		rlbl_menu.setFont(new Font("Arial", Font.BOLD, 14));
        	}

	        @Override
	        public void mouseExited(MouseEvent e) {

	        	rlbl_menu.setBackground(new Color(45, 61, 75));
	        	rlbl_menu.setForeground(new Color(255, 255, 255));
	        	rlbl_menu.setFont(new Font("Arial", Font.PLAIN, 14));
	        }
        });

        add(rlbl_menu);
    }
}
