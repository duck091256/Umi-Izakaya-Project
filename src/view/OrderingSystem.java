package view;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import fx.*;
import model.Staff;
import model.Table;
import service.Ordering;
import test.EnhancedChatClient;
import test.EnhancedChatServer;
import controller.*;
import data_access_object.StaffDAO;
import data_access_object.TableDAO;

public class OrderingSystem extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JLabel backgroundLabel;
    private JPanel cardPanelLeft, cardPanelRight;
    private JPanel floor1PanelLeft, floor1PanelRight;
    private JPanel floor2PanelLeft, floor2PanelRight;
    private CardLayout cardLayoutLeft, cardLayoutRight;
    private CardLayout subCardLayoutLeft, subCardLayoutRight;
    private JLayeredPane layeredPaneLeft, layeredPaneRight;
    private JPanel tablePanelLeft, tablePanelRight;
    private JPanel subCardPanelLeft, subCardPanelRight;
    private Point mouseClickPoint;
    private OperatingSystemController controller;
    private RoundedLabelEffect currentTableSeatLabel1, currentTableSeatLabel2;
    private int x1, y1, x2, y2;
    private Boolean checkTabExist1 = true, checkTabExist2 = true;
    private RoundedLabelEffect rlbl_menu, rlbl_table;
    private Boolean checkTabTable = false, checkTabMenu = true;
    private JLabel lbl_infor_Employee;
    private String username = "Employee";
    
    private JPanel tableAreaPanel1, Table1;	// Khu vực bàn tầng 1


    public void setAccountInformation(String Rusername) {
        this.username = Rusername;
        
        if (lbl_infor_Employee != null) {
        	lbl_infor_Employee.setText(Rusername);
        }
    }
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				OrderingSystem frame = new OrderingSystem();
				frame.setUndecorated(true); // Ẩn thanh tiêu đề
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public OrderingSystem() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 506);
		setLocationRelativeTo(null);
		contentPane = new JPanel(null);
		contentPane.setBorder(BorderFactory.createLineBorder(new Color(45, 61, 75), 5));
		setContentPane(contentPane);
		controller = new OperatingSystemController();
		
		// Khởi chạy server trong 1 thread riêng
        new Thread(() -> EnhancedChatServer.main(null)).start();
		
		// Thêm ảnh nền
		ImageIcon icon = new ImageIcon(getClass().getResource("/image/Order_Interface.png"));
		Image image = icon.getImage();
		Image scaledImage = image.getScaledInstance(900, 506, Image.SCALE_SMOOTH);

		// Rounded Label "Thực đơn"
		rlbl_menu = new RoundedLabelEffect("Thực đơn");
		rlbl_menu.setBounds(114, 11, 100, 31);
		rlbl_menu.setBackground(new Color(45, 61, 75));
		rlbl_menu.setForeground(new Color(255, 255, 255));
		rlbl_menu.setHorizontalAlignment(SwingConstants.CENTER);
		rlbl_menu.setFont(new Font("Arial", Font.PLAIN, 14));
		rlbl_menu.setCornerRadius(10);

		rlbl_menu.addMouseListener(new MouseAdapter() {
			@Override
            public void mouseClicked(MouseEvent e) {
				// Set màu tab phòng bàn
				rlbl_table.setBackground(new Color(45, 61, 75));
				rlbl_table.setForeground(new Color(255, 255, 255));
				rlbl_table.setFont(new Font("Arial", Font.PLAIN, 14));
				
                cardLayoutLeft.show(cardPanelLeft, "Thực đơn");
                checkTabMenu = false;
                checkTabTable = true;
            }

			@Override
			public void mouseEntered(MouseEvent e) {
				rlbl_menu.setBackground(new Color(255, 255, 255));
				rlbl_menu.setForeground(new Color(166, 166, 166));
				rlbl_menu.setFont(new Font("Arial", Font.BOLD, 14));
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				if (checkTabMenu) {
					rlbl_menu.setBackground(new Color(45, 61, 75));
					rlbl_menu.setForeground(new Color(255, 255, 255));
					rlbl_menu.setFont(new Font("Arial", Font.PLAIN, 14));
				}
			}
		});

		// Rounded Label "Phòng bàn"
		rlbl_table = new RoundedLabelEffect("Phòng bàn");
		rlbl_table.setBounds(10, 11, 100, 31);
		rlbl_table.setBackground(new Color(255, 255, 255));
		rlbl_table.setForeground(new Color(166, 166, 166));
		rlbl_table.setFont(new Font("Arial", Font.BOLD, 14));
		rlbl_table.setHorizontalAlignment(SwingConstants.CENTER);
		rlbl_table.setCornerRadius(10);

		rlbl_table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            	// Set màu tab menu
            	rlbl_menu.setBackground(new Color(45, 61, 75));
				rlbl_menu.setForeground(new Color(255, 255, 255));
				rlbl_menu.setFont(new Font("Arial", Font.PLAIN, 14));
				
                cardLayoutLeft.show(cardPanelLeft, "Phòng bàn");
                cardLayoutRight.show(cardPanelRight, "Phòng bàn");
                checkTabTable = false;
                checkTabMenu = true;
            }

			@Override
			public void mouseEntered(MouseEvent e) {
				rlbl_table.setBackground(new Color(255, 255, 255));
				rlbl_table.setForeground(new Color(166, 166, 166));
				rlbl_table.setFont(new Font("Arial", Font.BOLD, 14));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (checkTabTable) {
					rlbl_table.setBackground(new Color(45, 61, 75));
					rlbl_table.setForeground(new Color(255, 255, 255));
					rlbl_table.setFont(new Font("Arial", Font.PLAIN, 14));	
				}
			}
		});
		
		lbl_infor_Employee = new JLabel(username);
		lbl_infor_Employee.setForeground(new Color(255, 255, 255));
		lbl_infor_Employee.setFont(new Font("Arial", Font.PLAIN, 16));
		lbl_infor_Employee.setBounds(786, 11, 78, 21);
		contentPane.add(lbl_infor_Employee);
		
		contentPane.add(rlbl_table);
		contentPane.add(rlbl_menu);

		JLabel lblExit = new JLabel("");
		lblExit.setBounds(873, 2, 27, 40);
		contentPane.add(lblExit);
		lblExit.setBackground(new Color(240, 240, 240));
		lblExit.setIcon(new ImageIcon(OrderingSystem.class.getResource("/icon/icons8-logout-20.png")));
		controller.exit(lblExit);
		
		JLabel lbl_chat_icon = new JLabel("");
		lbl_chat_icon.setBounds(742, 1, 40, 40);
        lbl_chat_icon.setIcon(new ImageIcon(OrderingSystem.class.getResource("/icon/chat-icon-40-ordering.png")));
        contentPane.add(lbl_chat_icon);
        
        // Khi click vào lblChat, mở cửa sổ chat
        lbl_chat_icon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Khởi chạy client trong thread riêng
                new Thread(() -> {
                    EnhancedChatClient.main(new String[]{username});
                }).start();

            }
        });
		
		/* * * * Khu trái * * * */
		cardLayoutLeft = new CardLayout();
        cardPanelLeft = new JPanel(cardLayoutLeft);
        cardPanelLeft.setBounds(0, 43, 449, 463);
        contentPane.add(cardPanelLeft);
        
        // Panel chứa tất cả component bên trái
		tablePanelLeft = new JPanel(null);
		tablePanelLeft.setBackground(new Color(220, 240, 255, 150));

		// Layer để sắp xếp hiển thị
		layeredPaneLeft = new JLayeredPane();
		layeredPaneLeft.setBounds(0, 0, 449, 463);
		layeredPaneLeft.setLayout(null);
		
		// CardLayout phụ để chứa Panel tầng 1 và tầng 2
		subCardLayoutLeft = new CardLayout();
		subCardPanelLeft = new JPanel(subCardLayoutLeft);
		subCardPanelLeft.setBounds(0, 0, 449, 463);
		subCardPanelLeft.setBackground(new Color(255, 255, 255));
		
		// Khu vực tầng 1
		floor1PanelLeft = new JPanel(null);
		floor1PanelLeft.setBackground(new Color(255, 255, 255));
		
		// Khu vực bàn tầng 1
		tableAreaPanel1 = new JPanel();
		tableAreaPanel1.setBackground(Color.WHITE);
		tableAreaPanel1.setLayout(new GridLayout(0, 3, 10, 10));

        for (int i = 1; i <= 15; i++) {
            Table1 = new JPanel();
            Table1.setBackground(Color.WHITE);
            Table1.setPreferredSize(new Dimension(100, 100));
            Table1.setBorder(new RoundedBorderPanel(15, new Color(45, 61, 75), 1));
            JLabel lbl_tableNum1 = new JLabel("Bàn " + i + ": Trống");
            Table1.add(lbl_tableNum1);
            
            // Chức năng nhấn và hiển thị bàn sang màn hình tay phải
            int tableNumber = i;
            Table1.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                	
                	// Màu mè tí 
                	rlbl_menu.setBackground(new Color(255, 255, 255));
    				rlbl_menu.setForeground(new Color(166, 166, 166));
    				rlbl_menu.setFont(new Font("Arial", Font.BOLD, 14));
    				rlbl_table.setBackground(new Color(45, 61, 75));
					rlbl_table.setForeground(new Color(255, 255, 255));
					rlbl_table.setFont(new Font("Arial", Font.PLAIN, 14));
					checkTabMenu = false;
					checkTabTable = true;
                	
                	// Set viền bàn thành màu đỏ
                	Table1.setBorder(new RoundedBorderPanel(15, new Color(192, 32, 39), 2));
                	
                	// Set văn bản hiển thị trên ô của bàn đó
                	lbl_tableNum1.setText("Bàn " + tableNumber + ": Đang phục vụ");
                	
                	// Gọi trang menu
                	cardLayoutLeft.show(cardPanelLeft, "Thực đơn");
                	
                	// Xóa RoundedLabelEffect cũ nếu nó tồn tại
                	if (currentTableSeatLabel1 != null) {
                        contentPane.remove(currentTableSeatLabel1);
                    }
                	if (checkTabExist1) {
                		if (currentTableSeatLabel2 != null) {
                            // Tạo Tab Rounded Label "Phòng bàn" mới
                            RoundedLabelEffect rlbl_tableSeat = new RoundedLabelEffect("Bàn " + tableNumber + " - Tầng 1");
                            rlbl_tableSeat.setBounds(575, 11, 110, 60);
                            rlbl_tableSeat.setBackground(new Color(255, 255, 255));
                            rlbl_tableSeat.setForeground(new Color(166, 166, 166));
                            rlbl_tableSeat.setHorizontalAlignment(SwingConstants.CENTER);
                            rlbl_tableSeat.setFont(new Font("Arial", Font.BOLD, 14));
                            rlbl_tableSeat.setCornerRadius(10);
                            
                            // Lưu vị trí cố định của "Phòng bàn" lần tới
                            x1 = rlbl_tableSeat.getX();
                            y1 = rlbl_tableSeat.getY();
                            
                            // Thay đổi trạng thái checkTabExist
                            checkTabExist1 = false;
                            
                            currentTableSeatLabel1 = rlbl_tableSeat;

                            contentPane.add(rlbl_tableSeat);

                            contentPane.setComponentZOrder(backgroundLabel, contentPane.getComponentCount() - 1);
                            
                            contentPane.revalidate();
                            contentPane.repaint();

                            floor1PanelRight.removeAll();
                            JLabel tableLabelF1Right = new JLabel("Thông tin của Bàn " + tableNumber + " Tầng 1");
                            tableLabelF1Right.setBounds(20, 20, 300, 30);
                            tableLabelF1Right.setFont(new Font("Arial", Font.BOLD, 16));
                            floor1PanelRight.add(tableLabelF1Right);
                            floor1PanelRight.repaint();
                            floor1PanelRight.revalidate();
                    	} else {
                    		// Tạo Tab Rounded Label "Phòng bàn" mới
                            RoundedLabelEffect rlbl_tableSeat = new RoundedLabelEffect("Bàn " + tableNumber + " - Tầng 1");
                            rlbl_tableSeat.setBounds(460, 11, 110, 31);
                            rlbl_tableSeat.setBackground(new Color(255, 255, 255));
                            rlbl_tableSeat.setForeground(new Color(166, 166, 166));
                            rlbl_tableSeat.setHorizontalAlignment(SwingConstants.CENTER);
                            rlbl_tableSeat.setFont(new Font("Arial", Font.BOLD, 14));
                            rlbl_tableSeat.setCornerRadius(10);
                            
                            // Lưu vị trí cố định của "Phòng bàn" lần tới
                            x1 = rlbl_tableSeat.getX();
                            y1 = rlbl_tableSeat.getY();
                            
                            // Thay đổi trạng thái checkTabExist
                            checkTabExist1 = false;
                            
                            currentTableSeatLabel1 = rlbl_tableSeat;

                            contentPane.add(rlbl_tableSeat);

                            contentPane.setComponentZOrder(backgroundLabel, contentPane.getComponentCount() - 1);
                            
                            contentPane.revalidate();
                            contentPane.repaint();

                            floor1PanelRight.removeAll();
                            JLabel tableLabel = new JLabel("Thông tin của Bàn " + tableNumber + " Tầng 1");
                            tableLabel.setBounds(20, 20, 300, 30);
                            tableLabel.setFont(new Font("Arial", Font.BOLD, 16));
                            floor1PanelRight.add(tableLabel);
                            floor1PanelRight.repaint();
                            floor1PanelRight.revalidate();
                    	}
                	} else {
                		// Tạo Tab Rounded Label "Phòng bàn" mới
                        RoundedLabelEffect rlbl_tableSeat = new RoundedLabelEffect("Bàn " + tableNumber + " - Tầng 1");
                        rlbl_tableSeat.setBounds(x1, y1, 110, 31);
                        rlbl_tableSeat.setBackground(new Color(255, 255, 255));
                        rlbl_tableSeat.setForeground(new Color(166, 166, 166));
                        rlbl_tableSeat.setHorizontalAlignment(SwingConstants.CENTER);
                        rlbl_tableSeat.setFont(new Font("Arial", Font.BOLD, 14));
                        rlbl_tableSeat.setCornerRadius(10);
                        
                        currentTableSeatLabel1 = rlbl_tableSeat;

                        contentPane.add(rlbl_tableSeat);

                        contentPane.setComponentZOrder(backgroundLabel, contentPane.getComponentCount() - 1);
                        
                        contentPane.revalidate();
                        contentPane.repaint();

                        floor1PanelRight.removeAll();
                        JLabel tableLabel = new JLabel("Thông tin của Bàn " + tableNumber + " Tầng 1");
                        tableLabel.setBounds(20, 20, 300, 30);
                        tableLabel.setFont(new Font("Arial", Font.BOLD, 16));
                        floor1PanelRight.add(tableLabel);
                        floor1PanelRight.repaint();
                        floor1PanelRight.revalidate();
                	}
                }
            });
            
            tableAreaPanel1.add(Table1);
        }
        floor1PanelLeft.setLayout(null);

        JScrollPane scrollPane1 = new JScrollPane(tableAreaPanel1);
        scrollPane1.setBounds(10, 44, 439, 419);
        floor1PanelLeft.add(scrollPane1);

        // Khu vực tầng 2
		floor2PanelLeft = new JPanel(null);
		floor2PanelLeft.setBackground(new Color(255, 255, 255));

		// Khu vực bàn tầng 2
		JPanel tableAreaPanel2 = new JPanel();
		tableAreaPanel2.setBackground(Color.WHITE);
		tableAreaPanel2.setLayout(new GridLayout(0, 3, 10, 10));

		for (int i = 1; i <= 10; i++) {
            JPanel Table = new JPanel();
            Table.setBackground(Color.WHITE);
            Table.setPreferredSize(new Dimension(100, 100));
            Table.setBorder(new RoundedBorderPanel(15, new Color(45, 61, 75), 1));
            JLabel lbl_tableNum2 = new JLabel("Bàn " + i + ": Trống");
            Table.add(lbl_tableNum2);
            
            // Chức nâng nhấn và hiển thị bàn sang màn hình tay phải
            int tableNumber = i;
            Table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                	
                	// Màu mè tí 
                	rlbl_menu.setBackground(new Color(255, 255, 255));
    				rlbl_menu.setForeground(new Color(166, 166, 166));
    				rlbl_menu.setFont(new Font("Arial", Font.BOLD, 14));
    				rlbl_table.setBackground(new Color(45, 61, 75));
					rlbl_table.setForeground(new Color(255, 255, 255));
					rlbl_table.setFont(new Font("Arial", Font.PLAIN, 14));
					checkTabMenu = false;
					checkTabTable = true;
                	
                	// Set viền bàn thành màu đỏ
                	Table.setBorder(new RoundedBorderPanel(15, new Color(192, 32, 39), 2));
                	
                	// Set văn bản hiển thị trên ô của bàn đó
                	lbl_tableNum2.setText("Bàn " + tableNumber + ": Đang phục vụ");
                	
                	// Gọi trang menu
                	cardLayoutLeft.show(cardPanelLeft, "Thực đơn");
                    
                	// Xóa RoundedLabelEffect cũ nếu nó tồn tại
                    if (currentTableSeatLabel2 != null) {
                        contentPane.remove(currentTableSeatLabel2);
                    }
                    if (checkTabExist2) {
                    	if (currentTableSeatLabel1 != null) {
                            // Tạo Tab Rounded Label "Phòng bàn" mới
                            RoundedLabelEffect rlbl_tableSeat = new RoundedLabelEffect("Bàn " + tableNumber + " - Tầng 2");
                            rlbl_tableSeat.setBounds(575, 11, 110, 31);
                            rlbl_tableSeat.setBackground(new Color(255, 255, 255));
                            rlbl_tableSeat.setForeground(new Color(166, 166, 166));
                            rlbl_tableSeat.setHorizontalAlignment(SwingConstants.CENTER);
                            rlbl_tableSeat.setFont(new Font("Arial", Font.BOLD, 14));
                            rlbl_tableSeat.setCornerRadius(10);
                            
                            // Lưu vị trí cố định của "Phòng bàn" lần tới
                            x2 = rlbl_tableSeat.getX();
                            y2 = rlbl_tableSeat.getY();
                            
                            // Thay đổi trạng thái checkTabExist
                            checkTabExist2 = false;
                            
                            currentTableSeatLabel2 = rlbl_tableSeat;

                            contentPane.add(rlbl_tableSeat);

                            contentPane.setComponentZOrder(backgroundLabel, contentPane.getComponentCount() - 1);
                            
                            contentPane.revalidate();
                            contentPane.repaint();

                            floor2PanelRight.removeAll();
                            JLabel tableLabel = new JLabel("Thông tin của Bàn " + tableNumber + " Tầng 2");
                            tableLabel.setBounds(20, 20, 300, 30);
                            tableLabel.setFont(new Font("Arial", Font.BOLD, 16));
                            floor2PanelRight.add(tableLabel);
                            floor2PanelRight.repaint();
                            floor2PanelRight.revalidate();
                        } else {
                        	// Tạo Tab Rounded Label "Phòng bàn" mới
                            RoundedLabelEffect rlbl_tableSeat = new RoundedLabelEffect("Bàn " + tableNumber + " - Tầng 2");
                            rlbl_tableSeat.setBounds(460, 11, 110, 31);
                            rlbl_tableSeat.setBackground(new Color(255, 255, 255));
                            rlbl_tableSeat.setForeground(new Color(166, 166, 166));
                            rlbl_tableSeat.setHorizontalAlignment(SwingConstants.CENTER);
                            rlbl_tableSeat.setFont(new Font("Arial", Font.BOLD, 14));
                            rlbl_tableSeat.setCornerRadius(10);

                            // Lưu vị trí cố định của "Phòng bàn" lần tới
                            x2 = rlbl_tableSeat.getX();
                            y2 = rlbl_tableSeat.getY();
                            
                            // Thay đổi trạng thái checkTabExist
                            checkTabExist2 = false;
                            
                            currentTableSeatLabel2 = rlbl_tableSeat;

                            contentPane.add(rlbl_tableSeat);

                            contentPane.setComponentZOrder(backgroundLabel, contentPane.getComponentCount() - 1);
                            
                            contentPane.revalidate();
                            contentPane.repaint();

                            floor2PanelRight.removeAll();
                            JLabel tableLabel = new JLabel("Thông tin của Bàn " + tableNumber + " Tầng 2");
                            tableLabel.setBounds(20, 20, 300, 30);
                            tableLabel.setFont(new Font("Arial", Font.BOLD, 16));
                            floor2PanelRight.add(tableLabel);
                            floor2PanelRight.repaint();
                            floor2PanelRight.revalidate();
                        }
                    } else {
                    	// Tạo Tab Rounded Label "Phòng bàn" mới
                        RoundedLabelEffect rlbl_tableSeat = new RoundedLabelEffect("Bàn " + tableNumber + " - Tầng 2");
                        rlbl_tableSeat.setBounds(x2, y2, 110, 31);
                        rlbl_tableSeat.setBackground(new Color(255, 255, 255));
                        rlbl_tableSeat.setForeground(new Color(166, 166, 166));
                        rlbl_tableSeat.setHorizontalAlignment(SwingConstants.CENTER);
                        rlbl_tableSeat.setFont(new Font("Arial", Font.BOLD, 14));
                        rlbl_tableSeat.setCornerRadius(10);
                        
                        currentTableSeatLabel2 = rlbl_tableSeat;

                        contentPane.add(rlbl_tableSeat);

                        contentPane.setComponentZOrder(backgroundLabel, contentPane.getComponentCount() - 1);
                        
                        contentPane.revalidate();
                        contentPane.repaint();

                        floor2PanelRight.removeAll();
                        JLabel tableLabel = new JLabel("Thông tin của Bàn " + tableNumber + " Tầng 2");
                        tableLabel.setBounds(20, 20, 300, 30);
                        tableLabel.setFont(new Font("Arial", Font.BOLD, 16));
                        floor2PanelRight.add(tableLabel);
                        floor2PanelRight.repaint();
                        floor2PanelRight.revalidate();
                    }
                }
            });

            tableAreaPanel2.add(Table);
        }
        floor2PanelLeft.setLayout(null);

        JScrollPane scrollPanel2 = new JScrollPane(tableAreaPanel2);
        scrollPanel2.setBounds(10, 44, 439, 419);
        floor2PanelLeft.add(scrollPanel2);

		subCardPanelLeft.add(floor1PanelLeft, "Tầng 1");
		subCardPanelLeft.add(floor2PanelLeft, "Tầng 2");

		layeredPaneLeft.add(subCardPanelLeft, JLayeredPane.DEFAULT_LAYER);

		JComboBox<String> floorSelectorLeft = new JComboBox<>(new String[] { "Tầng 1", "Tầng 2" });
		floorSelectorLeft.setBounds(10, 10, 125, 25);
		floorSelectorLeft.setOpaque(true);
		floorSelectorLeft.setBackground(Color.WHITE);
		floorSelectorLeft.setForeground(Color.DARK_GRAY);
		floorSelectorLeft.setFont(new Font("Arial", Font.PLAIN, 14));

		tablePanelLeft.add(layeredPaneLeft);
		cardPanelLeft.add(tablePanelLeft, "Phòng bàn");

		// Panel "Thực đơn"
		JPanel menuPanelLeft = new JPanel(null);
		menuPanelLeft.setBackground(new Color(255, 255, 255));
		cardPanelLeft.add(menuPanelLeft, "Thực đơn");
		
		JPanel menuListPanelLeft = new JPanel();
		menuListPanelLeft.setBackground(Color.WHITE);
		menuListPanelLeft.setLayout(new GridLayout(0, 3, 10, 10));
		
		for (int i = 1; i <= 25; i++) {
            JPanel MenuList = new JPanel();
            MenuList.setBackground(Color.WHITE);
            MenuList.setPreferredSize(new Dimension(100, 130));
            MenuList.setBorder(new RoundedBorderPanel(15, new Color(45, 61, 75), 1));
            MenuList.add(new JLabel("Món " + i));
            menuListPanelLeft.add(MenuList);
            
            menuListPanelLeft.addMouseListener(new MouseAdapter () {
            	@Override
            	public void mouseClicked(MouseEvent e) {
//            		floor1PanelRight.removeAll();
//                    JLabel tableLabelF1Right = new JLabel("Thông tin của Bàn " + tableNumber + " Tầng 1");
//                    tableLabelF1Right.setBounds(20, 20, 300, 30);
//                    tableLabelF1Right.setFont(new Font("Arial", Font.BOLD, 16));
//                    floor1PanelRight.add(tableLabelF1Right);
//                    floor1PanelRight.repaint();
//                    floor1PanelRight.revalidate();
            	}
            });
        }
		menuPanelLeft.setLayout(null);

        JScrollPane scrollMenuPanel = new JScrollPane(menuListPanelLeft);
        scrollMenuPanel.setBounds(10, 11, 439, 452);
        menuPanelLeft.add(scrollMenuPanel);
		
        // * * * Khu phải * * *
        cardLayoutRight = new CardLayout();
        cardPanelRight = new JPanel(cardLayoutRight);
        cardPanelRight.setBounds(451, 43, 449, 463);
        contentPane.add(cardPanelRight);
        
		tablePanelRight = new JPanel(null);
		tablePanelRight.setBackground(new Color(220, 240, 255, 150));

		layeredPaneRight = new JLayeredPane();
		layeredPaneRight.setBounds(0, 0, 449, 463);
		layeredPaneLeft.setLayout(null);

		subCardLayoutRight = new CardLayout();
		subCardPanelRight = new JPanel(subCardLayoutRight);
		subCardPanelRight.setBounds(0, 0, 449, 463);
		subCardPanelRight.setBackground(new Color(240, 240, 240));

		floor1PanelRight = new JPanel(null);
		floor1PanelRight.setBackground(new Color(255, 255, 255));
		JLabel floor1LabelRight1 = new JLabel("Hiện không có bàn nào được chọn");
		floor1LabelRight1.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel floor1LabelRight2 = new JLabel("Nhấn vào biểu tượng bàn bên trái đến bắt đầu gọi món!");
		floor1LabelRight2.setHorizontalAlignment(SwingConstants.CENTER);
		
		floor1LabelRight1.setFont(new Font("Arial", Font.BOLD, 20));
		floor1LabelRight1.setBounds(10, 209, 429, 30);
		floor1PanelRight.add(floor1LabelRight1);
		floor1LabelRight2.setFont(new Font("Arial", Font.PLAIN, 16));
		floor1LabelRight2.setBounds(10, 234, 429, 30);
		floor1PanelRight.add(floor1LabelRight2);

		subCardPanelRight.add(floor1PanelRight, "Tầng 1");
		
		JLabel TableImage1 = new JLabel("");
		TableImage1.setIcon(new ImageIcon(OrderingSystem.class.getResource("/icon/icons8-table-50.png")));
		TableImage1.setBounds(199, 148, 50, 50);
		floor1PanelRight.add(TableImage1);
		
		floor2PanelRight = new JPanel(null);
		floor2PanelRight.setBackground(new Color(200, 250, 200));
		JLabel floor2LabelRight1 = new JLabel("Hiện không có bàn nào được chọn");
		floor2LabelRight1.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel floor2LabelRight2 = new JLabel("Nhấn vào biểu tượng bàn bên trái đến bắt đầu gọi món!");
		floor2LabelRight2.setHorizontalAlignment(SwingConstants.CENTER);
		
		floor2LabelRight1.setFont(new Font("Arial", Font.BOLD, 20));
		floor2LabelRight1.setBounds(10, 209, 429, 30);
		floor2PanelRight.add(floor2LabelRight1);
		floor2LabelRight2.setFont(new Font("Arial", Font.PLAIN, 16));
		floor2LabelRight2.setBounds(10, 234, 429, 30);
		floor2PanelRight.add(floor2LabelRight2);
		
		JLabel TableImage2 = new JLabel("");
		TableImage2.setIcon(new ImageIcon(OrderingSystem.class.getResource("/icon/icons8-table-50.png")));
		TableImage2.setBounds(199, 148, 50, 50);
		floor2PanelRight.add(TableImage2);

		subCardPanelRight.add(floor2PanelRight, "Tầng 2");

		layeredPaneRight.add(subCardPanelRight, JLayeredPane.DEFAULT_LAYER);

		tablePanelRight.add(layeredPaneRight);
		cardPanelRight.add(tablePanelRight, "Phòng bàn");
		
		// Panel "Thực đơn"
		JPanel menuPanelRight = new JPanel(null);
		menuPanelRight.setBackground(new Color(255, 240, 220, 150)); // Trong suốt nhẹ
		JLabel menuLabelRight = new JLabel("Thực đơn");
		menuLabelRight.setFont(new Font("Arial", Font.BOLD, 20));
		menuLabelRight.setBounds(10, 10, 200, 30);
		menuPanelRight.add(menuLabelRight);
		cardPanelRight.add(menuPanelRight, "Thực đơn");
		
		backgroundLabel = new JLabel(new ImageIcon(scaledImage));
		backgroundLabel.setBounds(0, -1, 900, 506);
		contentPane.add(backgroundLabel);
		contentPane.setBorder(BorderFactory.createLineBorder(new Color(45, 61, 75), 5));
		
		// Bo viền
		floorSelectorLeft.setBorder(new RoundedBorder(10));

		// Sự kiện đổi tầng
		floorSelectorLeft.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				String selectedFloor = (String) e.getItem();
				subCardLayoutLeft.show(subCardPanelLeft, selectedFloor);
				subCardLayoutRight.show(subCardPanelRight, selectedFloor);
			}
		});

		// Thêm vào JLayeredPane
		layeredPaneLeft.add(floorSelectorLeft, JLayeredPane.POPUP_LAYER);
		
		// Đặt ảnh nền phía sau các thành phần khác
		contentPane.setComponentZOrder(backgroundLabel, contentPane.getComponentCount() - 1);

		// Chức năng di chuyển cửa sổ
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				mouseClickPoint = e.getPoint();
				setOpacity(0.6f);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				setOpacity(1.0f);
			}
		});

		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				Point currentPoint = e.getLocationOnScreen();
				setLocation(currentPoint.x - mouseClickPoint.x, currentPoint.y - mouseClickPoint.y);
			}
		});
	}
	
    private void loadFloor() {
        StaffDAO.loadData();
        TableDAO.loadData();
        
        for (Table table : TableDAO.list) {
        	if (table.getTableID().contains("#T1")) {
        		// Số thứ tự của bàn
            	int i = 0;
            	i++;
            	
                String res = table.getResponsibleBy();
                Staff staff = StaffDAO.getStaff(res);
                
                String name = "";
                if (staff == null) {
                    name = "";
                } else {
                    name = staff.getFullName();
                }
                
                // Kiểm tra trạng thái bàn dựa trên orderList
                String status = Ordering.orderList.containsKey(table.getTableID()) ? 
                               "Đang phục vụ" : table.getOperatingStatus();
                
                Table1 = new JPanel();
                Table1.setBackground(Color.WHITE);
                Table1.setPreferredSize(new Dimension(100, 100));
                Table1.setBorder(new RoundedBorderPanel(15, new Color(45, 61, 75), 1));
                JLabel lbl_tableNum1 = new JLabel("Bàn " + i + ": Trống");
                Table1.add(lbl_tableNum1);
                
                // Chức năng nhấn và hiển thị bàn sang màn hình tay phải
                int tableNumber = i;
                Table1.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                    	
                    	// Màu mè tí 
                    	rlbl_menu.setBackground(new Color(255, 255, 255));
        				rlbl_menu.setForeground(new Color(166, 166, 166));
        				rlbl_menu.setFont(new Font("Arial", Font.BOLD, 14));
        				rlbl_table.setBackground(new Color(45, 61, 75));
    					rlbl_table.setForeground(new Color(255, 255, 255));
    					rlbl_table.setFont(new Font("Arial", Font.PLAIN, 14));
    					checkTabMenu = false;
    					checkTabTable = true;
                    	
                    	// Set viền bàn thành màu đỏ
                    	Table1.setBorder(new RoundedBorderPanel(15, new Color(192, 32, 39), 2));
                    	
                    	// Set văn bản hiển thị trên ô của bàn đó
                    	lbl_tableNum1.setText("Bàn " + tableNumber + ": Đang phục vụ");
                    	
                    	// Gọi trang menu
                    	cardLayoutLeft.show(cardPanelLeft, "Thực đơn");
                    	
                    	// Xóa RoundedLabelEffect cũ nếu nó tồn tại
                    	if (currentTableSeatLabel1 != null) {
                            contentPane.remove(currentTableSeatLabel1);
                        }
                    	if (checkTabExist1) {
                    		if (currentTableSeatLabel2 != null) {
                                // Tạo Tab Rounded Label "Phòng bàn" mới
                                RoundedLabelEffect rlbl_tableSeat = new RoundedLabelEffect("Bàn " + tableNumber + " - Tầng 1");
                                rlbl_tableSeat.setBounds(575, 11, 110, 60);
                                rlbl_tableSeat.setBackground(new Color(255, 255, 255));
                                rlbl_tableSeat.setForeground(new Color(166, 166, 166));
                                rlbl_tableSeat.setHorizontalAlignment(SwingConstants.CENTER);
                                rlbl_tableSeat.setFont(new Font("Arial", Font.BOLD, 14));
                                rlbl_tableSeat.setCornerRadius(10);
                                
                                // Lưu vị trí cố định của "Phòng bàn" lần tới
                                x1 = rlbl_tableSeat.getX();
                                y1 = rlbl_tableSeat.getY();
                                
                                // Thay đổi trạng thái checkTabExist
                                checkTabExist1 = false;
                                
                                currentTableSeatLabel1 = rlbl_tableSeat;

                                contentPane.add(rlbl_tableSeat);

                                contentPane.setComponentZOrder(backgroundLabel, contentPane.getComponentCount() - 1);
                                
                                contentPane.revalidate();
                                contentPane.repaint();

                                floor1PanelRight.removeAll();
                                JLabel tableLabelF1Right = new JLabel("Thông tin của Bàn " + tableNumber + " Tầng 1");
                                tableLabelF1Right.setBounds(20, 20, 300, 30);
                                tableLabelF1Right.setFont(new Font("Arial", Font.BOLD, 16));
                                floor1PanelRight.add(tableLabelF1Right);
                                floor1PanelRight.repaint();
                                floor1PanelRight.revalidate();
                        	} else {
                        		// Tạo Tab Rounded Label "Phòng bàn" mới
                                RoundedLabelEffect rlbl_tableSeat = new RoundedLabelEffect("Bàn " + tableNumber + " - Tầng 1");
                                rlbl_tableSeat.setBounds(460, 11, 110, 31);
                                rlbl_tableSeat.setBackground(new Color(255, 255, 255));
                                rlbl_tableSeat.setForeground(new Color(166, 166, 166));
                                rlbl_tableSeat.setHorizontalAlignment(SwingConstants.CENTER);
                                rlbl_tableSeat.setFont(new Font("Arial", Font.BOLD, 14));
                                rlbl_tableSeat.setCornerRadius(10);
                                
                                // Lưu vị trí cố định của "Phòng bàn" lần tới
                                x1 = rlbl_tableSeat.getX();
                                y1 = rlbl_tableSeat.getY();
                                
                                // Thay đổi trạng thái checkTabExist
                                checkTabExist1 = false;
                                
                                currentTableSeatLabel1 = rlbl_tableSeat;

                                contentPane.add(rlbl_tableSeat);

                                contentPane.setComponentZOrder(backgroundLabel, contentPane.getComponentCount() - 1);
                                
                                contentPane.revalidate();
                                contentPane.repaint();

                                floor1PanelRight.removeAll();
                                JLabel tableLabel = new JLabel("Thông tin của Bàn " + tableNumber + " Tầng 1");
                                tableLabel.setBounds(20, 20, 300, 30);
                                tableLabel.setFont(new Font("Arial", Font.BOLD, 16));
                                floor1PanelRight.add(tableLabel);
                                floor1PanelRight.repaint();
                                floor1PanelRight.revalidate();
                        	}
                    	} else {
                    		// Tạo Tab Rounded Label "Phòng bàn" mới
                            RoundedLabelEffect rlbl_tableSeat = new RoundedLabelEffect("Bàn " + tableNumber + " - Tầng 1");
                            rlbl_tableSeat.setBounds(x1, y1, 110, 31);
                            rlbl_tableSeat.setBackground(new Color(255, 255, 255));
                            rlbl_tableSeat.setForeground(new Color(166, 166, 166));
                            rlbl_tableSeat.setHorizontalAlignment(SwingConstants.CENTER);
                            rlbl_tableSeat.setFont(new Font("Arial", Font.BOLD, 14));
                            rlbl_tableSeat.setCornerRadius(10);
                            
                            currentTableSeatLabel1 = rlbl_tableSeat;

                            contentPane.add(rlbl_tableSeat);

                            contentPane.setComponentZOrder(backgroundLabel, contentPane.getComponentCount() - 1);
                            
                            contentPane.revalidate();
                            contentPane.repaint();

                            floor1PanelRight.removeAll();
                            JLabel tableLabel = new JLabel("Thông tin của Bàn " + tableNumber + " Tầng 1");
                            tableLabel.setBounds(20, 20, 300, 30);
                            tableLabel.setFont(new Font("Arial", Font.BOLD, 16));
                            floor1PanelRight.add(tableLabel);
                            floor1PanelRight.repaint();
                            floor1PanelRight.revalidate();
                    	}
                    }
                });
                
                tableAreaPanel1.add(Table1);
        	}
        	
        }
    }
}