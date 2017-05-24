import java.sql.*;
import java.util.HashMap;
import java.util.*;
import java.sql.Date;

public class Database {
	public static ArrayList<String> processedExams=new ArrayList<String>();
	public static ArrayList<String> examsLeft=new ArrayList<String>();
	public static Connection con=null;
	private String url="jdbc:db2://localhost:50001/vstud";
	private String userId="student";
	private String password="abcdef";
	static{
		try{
			Class.forName("com.ibm.db2.jcc.DB2Driver");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public Database(){
		try{
			con=DriverManager.getConnection(url,userId,password);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static int registeredYears(int index){
		try{
			Statement stmt=con.createStatement();
			String sql="select count(*) from upis_godine where indeks="+index;
			ResultSet rs=stmt.executeQuery(sql);
			int years=0;
			while(rs.next()){
				years=rs.getInt(1);
			}
			rs.close();
			stmt.close();
			return years;
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}
	}
	public static String unpassedExams(int indeks){
		try{
			String sql="select distinct p.naziv,op.id_predmeta,d.indeks from obavezan_predmet op"+
					" join predmet p on p.id_predmeta=op.id_predmeta " +
					" join dosije d on d.id_smera=op.id_smera"+
					" where d.indeks=? and not exists(select * " +
					" from ispit i"+
					" where i.id_predmeta=op.id_predmeta and ocena>5 and " +
					" status_prijave='o') and p.naziv not in (" +
					" select naziv_predmeta from jun_2015)";
			PreparedStatement pstmt=con.prepareStatement(sql);
			pstmt.setInt(1,indeks);
			ResultSet rs=pstmt.executeQuery();
			String res="<html>";
			int i=0;
			while(rs.next()){
				try{
					examsLeft.add(rs.getString(1));
					res+=(++i)+". "+rs.getString(1)+"<br>";
				}catch(SQLException e){
					if(e.getErrorCode()==-911 || e.getErrorCode()==-913)
						rs=processHold(rs,con,pstmt);
				}
			}
			res+="</html>";
			con.commit();
			rs.close();
			pstmt.close();
			if(i==0) res="Nema ispita";
			return res;
		}catch(SQLException e){
			System.out.println("SQL error: SQLCODE"+e.getErrorCode());
			return null;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public static int registerExam(int index,String exam){
		int i=0;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		String sql="select * from jun_2015";
		Boolean processed=true;
		try{
			if(!processedExams.contains(exam)){
				pstmt=con.prepareStatement(sql,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
				rs=pstmt.executeQuery();
				rs.moveToInsertRow();
				rs.updateInt("indeks",index);
				rs.updateString("naziv_predmeta",exam);
				rs.insertRow();
				rs.moveToCurrentRow();
				processedExams.add(exam);
				if(processed){
					con.commit();
					examsLeft=new ArrayList<String>();
				}
				processed=true;
			}
		}catch(SQLException e){
			if(e.getErrorCode()==-911 || e.getErrorCode()==-913){
				try{
					rs.close();
					rs=processHoldForUpdate(rs,con,pstmt);
					processed=false;
				}catch(Exception e1){
					e1.printStackTrace();
					return -1;
				}
			}
		}
		return i;
	}
	private static ResultSet processHold(ResultSet rs,Connection con,PreparedStatement ps) throws SQLException{
		System.out.println("locked by other transaction! wait!");
		con.rollback();
		System.out.println("rollback!");
		return openCursor(ps);
	}
	private static ResultSet processHoldForUpdate(ResultSet rs,Connection con,PreparedStatement ps) throws SQLException{
		System.out.println("locked by other transaction! wait!");
		con.rollback();
		System.out.println("rollback!");
		return openCursorForUpdate(ps);
	}
	private static ResultSet openCursorForUpdate(PreparedStatement ps) throws SQLException{
		ResultSet rs=ps.executeQuery();
		rs.moveToInsertRow();
		return rs;
	}
	private static ResultSet openCursor(PreparedStatement ps) throws SQLException{
		ResultSet rs=ps.executeQuery();
		return rs;
	}
}
