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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;



public class EmployeeFrame extends JFrame{
	
	
	private static final int FRAME_WIDTH = 600;
	private static final int FRAME_HEIGHT = 800;
	private JLabel welcome;
	private JLabel menu;
	private JLabel genre;
	private JLabel dish;
	private Connection conn;
	private JTextField nameField;
	private JTextField numField;
	private PreparedStatement loginStmt;
	private JPanel orderPanel;
	private JPanel userPanel;
	private JPanel middlePanel;
	

	
	public EmployeeFrame() {
		this.setLayout(new BorderLayout());
		welcome = new JLabel();
		welcome.setText("Order Management System");
		this.add(welcome,BorderLayout.NORTH);
		
		middlePanel = new JPanel();
		userPanel = new JPanel();
		
		JLabel custName = new JLabel("username:");
		nameField = new JTextField(10);
		JLabel pNum = new JLabel("password:");
		numField = new JTextField(10);
		userPanel.add(custName);
		userPanel.add(nameField);
		userPanel.add(pNum);
		userPanel.add(numField);
		
		
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:restaurant.db");
			loginStmt = conn.prepareStatement("select password from Employee where username =?;");
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JButton loginButton = new JButton();
		loginButton.setText("Log in");
		loginButton.addActionListener(new LogInListener());
		userPanel.add(loginButton);
		userPanel.setVisible(true);
		middlePanel.add(userPanel);
		
		
		this.add(middlePanel,BorderLayout.CENTER);
		
		orderPanel = new JPanel();
		orderPanel.setLayout(new BoxLayout(orderPanel, BoxLayout.Y_AXIS));
		this.add(orderPanel,BorderLayout.SOUTH);
	}
	
	void createOrderPanel() {
		orderPanel.removeAll();
		orderPanel.setVisible(true);
		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement("select * from OrderInfo;");
			ResultSet rset = stmt.executeQuery();
			while (rset.next()){
				JPanel infoPanel = new JPanel();
				infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
				JLabel info = new JLabel();
				info.setText("Order ID:"+rset.getObject(1).toString()+"\t Customer Name:"+rset.getObject(2).toString()+"\n"
						+ "\tPhone#:"+rset.getObject(3).toString()+"\tStatus:"+rset.getObject(4).toString()+
						"\n");
				infoPanel.add(info);
				JLabel content = new JLabel();
				content.setText("Contents:\n");
				infoPanel.add(content);
				stmt = conn.prepareStatement("select * from OrderItem where OID = ?;");
				stmt.setInt(1, (Integer)rset.getObject(1));
				ResultSet orderSet = stmt.executeQuery();
				double total = 0;
				while(orderSet.next()) {
					JPanel dishPanel = new JPanel();
					
					dish = new JLabel();
					PreparedStatement dishStmt = conn.prepareStatement("select * from dish where DID = ?;");
					dishStmt.setInt(1, (Integer)orderSet.getObject(2));
					ResultSet dishSet = dishStmt.executeQuery();
					dish.setText(dishSet.getObject(2).toString());
					dishPanel.add(dish);
					total +=(double)dishSet.getObject(3);
				//orderButton.setName(rset.getObject(1).toString());
					infoPanel.add(dishPanel);
				}
				JLabel sum = new JLabel();
				sum.setText("Sub total:"+total);
				infoPanel.add(sum);
				JPanel actionPanel = new JPanel();
				JLabel actionl = new JLabel();
				actionl.setText("Action:");
				actionPanel.add(actionl);
				JComboBox action = new JComboBox();
				action.addItem("In Progress");
				action.addItem("Complete");
				action.addItem("Cancel");
				action.addActionListener(new comboListener());
				action.putClientProperty("id", rset.getObject(1));
				actionPanel.add(action);
				
				infoPanel.add(actionPanel);
				orderPanel.add(infoPanel);
				
			}
			orderPanel.repaint();
		}catch(SQLException e) {
				
			}
	}
	
	class comboListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			JComboBox source = (JComboBox)e.getSource();
            Integer id = (Integer) source.getClientProperty("id");
            String action = source.getSelectedItem().toString();
            	try {
            		if(action.equals("In Progress")) {
            			PreparedStatement ps = conn.prepareStatement("update orderinfo set status = \"In Progress\" where oid = ?;");
                		ps.setInt(1, id);
                		ps.executeUpdate();
                		orderPanel.setVisible(false);
                		createOrderPanel();
            		}
            		else if (action.equals("Complete")) {
					PreparedStatement ps = conn.prepareStatement("update orderinfo set status = \"Completed\" where oid = ?;");
            		ps.setInt(1, id);
            		ps.executeUpdate();
            		orderPanel.setVisible(false);
            		createOrderPanel();

            		}
            		else if (action.equals("Cancel")) {
            			PreparedStatement ps = conn.prepareStatement("update orderinfo set status = \"Cancelled\" where oid = ?;");
            			ps.setInt(1, id);
                		ps.executeUpdate();
                		orderPanel.setVisible(false);
                		createOrderPanel();

            		}
            		} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            
		}
		
	}
	class LogInListener implements ActionListener {
		   public void actionPerformed(ActionEvent event) {
			   String username = nameField.getText();
			   String password = numField.getText();
			   if(username.isEmpty() || password.isEmpty() ) {
				   JOptionPane.showMessageDialog(null,"All fields must be filled!");
			   }
			   else {
			   try {
				loginStmt.setString(1,username);

				ResultSet rset = loginStmt.executeQuery();
				if(rset.next()) {
					if(password.equals((String)rset.getObject(1))) {
					createOrderPanel();
					userPanel.setVisible(false);
					JLabel greeting = new JLabel();
					greeting.setText("Welcome,"+username);
					middlePanel.add(greeting);
					JButton outButton = new JButton();
					outButton.setText("Log out");
					outButton.addActionListener(new OutListener());
					
					middlePanel.add(greeting);
					middlePanel.add(outButton);
					middlePanel.repaint();
				}
					else {
						JOptionPane.showMessageDialog(null,"username/pw incorrect!");
					}
				}
				
				else {
					JOptionPane.showMessageDialog(null,"username/pw incorrect!");
				}
				
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   }
		   }
	   }
	
	class OutListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			userPanel.setVisible(true);
			orderPanel.setVisible(false);
			middlePanel.removeAll();
			middlePanel.add(userPanel);
			
		}
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame frame = new EmployeeFrame();
		   frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		   frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		   frame.setVisible(true);      
		
	}
}
