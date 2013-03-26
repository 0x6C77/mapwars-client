package uk.ac.aber.luw9.mapwars;

public class User {
	private int user_id;
	private String sess;
	
	public User(int user_id) {
		this.user_id = user_id;
	}
	
	public void setSession(String sess) {
		this.sess = sess;
	}
	
	public int getUserId() {
		return user_id;
	}
	
	public String getSession() {
		return sess;
	}
}
