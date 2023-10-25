import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class MainFrame extends JFrame{
	
	private static final int FRAME_WIDTH = 600;
	private static final int FRAME_HEIGHT = 800;
	private JLabel welcome;
	private JLabel menu;
	private JLabel genre;
	private JLabel dish;
	private Connection conn;
	private JTextField nameField;
	private JTextField numField;
	private PreparedStatement insertStmt;
	private JPanel menuPanel;
	private JPanel userPanel;
	private JPanel cartPanel;
	private int start = 0;
	private JButton cartButton;
	private JButton menuButton;
	private JButton refreshButton;
	private int orderId;

	public MainFrame() {
		// TODO Auto-generated constructor stub
		this.setLayout(new BorderLayout());
		welcome = new JLabel();
		welcome.setText("Welcome to my restaurant!");
		this.add(welcome,BorderLayout.NORTH);
		menu = new JLabel();
		menu.setText("Menu");
		
		JPanel middlePanel = new JPanel();
		userPanel = new JPanel();
		
		JLabel custName = new JLabel("Customer Name:");
		nameField = new JTextField(10);
		JLabel pNum = new JLabel("Phone #:");
		numField = new JTextField(10);
		userPanel.add(custName);
		userPanel.add(nameField);
		userPanel.add(pNum);
		userPanel.add(numField);
		
		
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:restaurant.db");
			insertStmt = conn.prepareStatement("insert into orderInfo(custName,pNum) values(?,?)");
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JButton insertButton = new JButton();
		insertButton.setText("Start Order");
		insertButton.addActionListener(new InsertButtonListener());
		userPanel.add(insertButton);
		userPanel.setVisible(true);
		middlePanel.add(userPanel);
		cartButton = new JButton();
		cartButton.setText("View Cart");
		cartButton.addActionListener(new CartListener());
		cartButton.setVisible(false);
		middlePanel.add(cartButton);
		
		
		menuButton = new JButton();
		menuButton.setText("View Menu");
		menuButton.addActionListener(new MenuListener());
		menuButton.setVisible(false);
		middlePanel.add(menuButton);
		
		this.add(middlePanel,BorderLayout.CENTER);
		menuPanel = new JPanel();
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
		menuPanel.add(menu);
		
		String[] types = {"Appetizer","Rice/Noodles","Entree","Vegetables","Beverages","Extras"};
		try {
			//conn = DriverManager.getConnection("jdbc:sqlite:restaurant.db");
			PreparedStatement stmt = conn.prepareStatement("select * from Dish where type = ?;");
			for(String type:types) {
				System.out.print(type+":\n");
				genre = new JLabel();
				genre.setText(type);
				menuPanel.add(genre);
			stmt.setString(1, type);
			ResultSet rset = stmt.executeQuery();

			while (rset.next()){
				JPanel dishPanel = new JPanel();

				dish = new JLabel();
				dish.setText(rset.getObject(2).toString()+"......"+rset.getObject(3).toString());
				dishPanel.add(dish);
				JButton orderButton = new JButton();
				orderButton.setText("Select");
				//orderButton.setName(rset.getObject(1).toString());
				orderButton.putClientProperty("id", rset.getObject(1));
				orderButton.addActionListener(new OrderListener());
				dishPanel.add(orderButton);
				menuPanel.add(dishPanel);
			}
			
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.add(menuPanel,BorderLayout.SOUTH);
		cartPanel = new JPanel();
		cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS));
		cartPanel.setVisible(false);
		this.add(cartPanel,BorderLayout.EAST);
		
	}
	class InsertButtonListener implements ActionListener {
		   public void actionPerformed(ActionEvent event) {

			   String custName = nameField.getText();
			   String pNum = numField.getText();
			   if(custName.isEmpty() || pNum.isEmpty() ) {
				   JOptionPane.showMessageDialog(null,"All fields must be filled!");
			   }
			   else {
			   try {
				insertStmt.setString(1,custName);
				insertStmt.setString(2,pNum);

				insertStmt.executeUpdate();
				
				PreparedStatement stmt = conn.prepareStatement("SELECT oid FROM orderinfo ORDER BY oid DESC LIMIT 1;");
				ResultSet rset = stmt.executeQuery();
				orderId = (Integer)rset.getObject(1);
				
				userPanel.setVisible(false);
				start = 1;
				cartButton.setVisible(true);
				
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   }
		   }
	   }
	class CartListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
			menuPanel.setVisible(false);
			cartButton.setVisible(false);
			menuButton.setVisible(true);
			cartPanel.setVisible(true);
			//cartPanel.add(new JButton());
			cartPanel.removeAll();
			PreparedStatement stmt;
			try {
				stmt = conn.prepareStatement("select * from OrderItem where OID = ?;");
				stmt.setInt(1, orderId);
				ResultSet rset = stmt.executeQuery();
				double total = 0;
				while (rset.next()){
					JPanel dishPanel = new JPanel();
					dish = new JLabel();
					PreparedStatement dishStmt = conn.prepareStatement("select * from dish where DID = ?;");
					dishStmt.setInt(1, (Integer)rset.getObject(2));
					ResultSet dishSet = dishStmt.executeQuery();
					dish.setText(dishSet.getObject(2).toString()+"......"+dishSet.getObject(3).toString());
					dishPanel.add(dish);
					total +=(double)dishSet.getObject(3);
					JButton deleteButton = new JButton();
					deleteButton.setText("Delete");
					//orderButton.setName(rset.getObject(1).toString());
					deleteButton.putClientProperty("id", rset.getObject(1));
					deleteButton.addActionListener(new DeleteListener());
					dishPanel.add(deleteButton);
					cartPanel.add(dishPanel);
				}
				JLabel sum = new JLabel();
				sum.setText("Total......"+total);
				cartPanel.add(sum);
				cartPanel.repaint();
			
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		
	}}
	
	class MenuListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			menuPanel.setVisible(true);
			cartButton.setVisible(true);
			menuButton.setVisible(false);
			cartPanel.setVisible(false);
			
			
		}
		
	}
	
	class OrderListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if(start==0) {
				JOptionPane.showMessageDialog(null,"Please Start Order first!");
			}
			else {
				JButton source = (JButton)e.getSource();
	            Integer id = (Integer) source.getClientProperty("id");
	            PreparedStatement newItem;
				try {
					newItem = conn.prepareStatement("Insert into orderItem(DID,OID) Values(?,?);");
					newItem.setInt(1, id);
					newItem.setInt(2, orderId);
					newItem.executeUpdate();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
			
		}
		
	}
	
	class DeleteListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

				JButton source = (JButton)e.getSource();
	            Integer id = (Integer) source.getClientProperty("id");
	            PreparedStatement toDelete;
				try {
					toDelete = conn.prepareStatement("Delete from orderItem where iid=?;");
					toDelete.setInt(1, id);
					toDelete.executeUpdate();
					System.out.print("Delete from orderItem where iid="+id+";");
					//menuButton.doClick();
					cartPanel.setVisible(false);
					cartButton.doClick();
						
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
			
		}
		
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame frame = new MainFrame();
		   frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		   frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		   frame.setVisible(true);      
		
	}
}

