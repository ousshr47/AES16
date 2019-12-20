/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author DrWalid
 */
public class Session {

	private static int id;
	private static Session ses;

	public static int getId() {
		return id;
	}

	public static void setId(int id) {
		Session.id = id;
	}

	public static Session GetInstance() {
		return ses;
	}

	public Session() {
	}

}
