package Netspan.Profiles;

/**
 * 
 * @author doguz
 *	Legend: There are wasn't differ in Connected UE traffic direction till NBI 17.0
 *  From NBI 17.0 Connected UE differ by Upload & Download 
 */
public enum ConnectedUETrafficDirection {
	ALL, UL, DL;

	public static ConnectedUETrafficDirection FromString(String tdirection) {
		return ConnectedUETrafficDirection.valueOf(tdirection.toUpperCase());
	}
}
