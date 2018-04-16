package Utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * The Class InetAddressesHelper.
 */
public class InetAddressesHelper {

	/** The Constant IPV4_PART_COUNT. */
	private static final int IPV4_PART_COUNT = 4;
	
	/** The Constant IPV6_PART_COUNT. */
	private static final int IPV6_PART_COUNT = 8;
	
	/*
	 * Returns the {@link InetAddress} having the given string representation. <p>This
	 * deliberately avoids all nameservice lookups (e.g. no DNS).
	 * @param ipString {@code String} containing an IPv4 or IPv6 string literal, e.g. {@code
	 * "192.168.0.1"} or {@code "2001:db8::1"}
	 * @return {@link InetAddress} representing the argument
	 * @throws IllegalArgumentException if the argument is not a valid IP string literal
	 */
	/**
	 * For string.
	 *
	 * @param ipString the ip string
	 * @return the inet address
	 */
	public static InetAddress forString( String ipString ) {
		byte[] addr = ipStringToBytes( ipString );

		// The argument was malformed, i.e. not an IP string literal.
		if ( addr == null ) {
			throw new IllegalArgumentException( String.format( "'%s' is not an IP string literal.", ipString ) );
		}

		return bytesToInetAddress( addr );
	}

	/**
	 * Returns a new InetAddress that is one more than the passed in address. This method works
	 * for both IPv4 and IPv6 addresses.
	 *
	 * @param address the InetAddress to increment
	 * @return a new InetAddress that is one more than the passed in address. if no incremant
	 *         is possible null will return.
	 * @since 10.0
	 */
	public static InetAddress increment( InetAddress address ) {
		byte[] addr = address.getAddress();
		int i = addr.length - 1;
		while ( i >= 0 && addr[i] == ( byte ) 0xff ) {
			addr[i] = 0;
			i--;
		}

		if ( i < 0 )
			return null;

		addr[i]++;
		return bytesToInetAddress( addr );
	}

	/*
	 * Returns true if the supplied string is a valid IP string literal, false otherwise
	 */
	/**
	 * Checks if is inet address.
	 *
	 * @param ipString the ip string
	 * @return true, if is inet address
	 */
	public static boolean isInetAddress( String ipString ) {
		return ipStringToBytes( ipString ) != null;
	}
	
	/**
	 * Convert string to addr.
	 *
	 * @param ip the ip
	 * @return the inet address
	 * @throws UnknownHostException the unknown host exception
	 */
	public static InetAddress convertStringToAddr(String ip) throws UnknownHostException{
		return InetAddress.getByName(ip);
	}

	/**
	 * Returns the string representation of an {@link InetAddress}.
	 *
	 * @param ip the ip
	 * @return the string
	 */
	public static String toAddrString( InetAddress ip ) {
		return ip.getHostAddress();
	}

	/**
	 * Convert a byte array into an InetAddress. {@link InetAddress#getByAddress} is documented
	 * as throwing a checked exception "if IP address if of illegal length." We replace it with
	 * an unchecked exception, for use by callers who already know that addr is an array of
	 * length 4 or 16.
	 * 
	 * @param addr the raw 4-byte or 16-byte IP address in big-endian order
	 * @return an InetAddress object created from the raw IP address
	 */
	private static InetAddress bytesToInetAddress( byte[] addr ) {
		try {
			return InetAddress.getByAddress( addr );
		} catch ( UnknownHostException e ) {
			throw new AssertionError( e );
		}
	}

	/**
	 * Convert dotted quad to hex.
	 *
	 * @param ipString the ip string
	 * @return the string
	 */
	private static String convertDottedQuadToHex( String ipString ) {
		int lastColon = ipString.lastIndexOf( ':' );
		String initialPart = ipString.substring( 0, lastColon + 1 );
		String dottedQuad = ipString.substring( lastColon + 1 );
		byte[] quad = textToNumericFormatV4( dottedQuad );
		if ( quad == null ) {
			return null;
		}
		String penultimate = Integer.toHexString( ( ( quad[0] & 0xff ) << 8 ) | ( quad[1] & 0xff ) );
		String ultimate = Integer.toHexString( ( ( quad[2] & 0xff ) << 8 ) | ( quad[3] & 0xff ) );
		return initialPart + penultimate + ":" + ultimate;
	}
	
	/**
	 * Ip string to bytes.
	 *
	 * @param ipString the ip string
	 * @return the byte[]
	 */
	public static byte[] ipStringToBytes( String ipString ) {
		// Make a first pass to categorize the characters in this string.
		boolean hasColon = false;
		boolean hasDot = false;
		if(ipString==null)
			return null;
		for ( int i = 0; i < ipString.length(); i++ ) {
			char c = ipString.charAt( i );
			if ( c == '.' ) {
				hasDot = true;
			} else if ( c == ':' ) {
				if ( hasDot ) {
					return null; // Colons must not appear after dots.
				}
				hasColon = true;
			} else if ( Character.digit( c, 16 ) == -1 ) {
				return null; // Everything else must be a decimal or hex digit.
			}
		}

		// Now decide which address family to parse.
		if ( hasColon ) {
			if ( hasDot ) {
				ipString = convertDottedQuadToHex( ipString );
				if ( ipString == null ) {
					return null;
				}
			}
			return textToNumericFormatV6( ipString );
		} else if ( hasDot ) {
			return textToNumericFormatV4( ipString );
		}
		return null;
	}

	/**
	 * Parses the hextet.
	 *
	 * @param ipPart the ip part
	 * @return the short
	 */
	private static short parseHextet( String ipPart ) {
		// Note: we already verified that this string contains only hex digits.
		int hextet = Integer.parseInt( ipPart, 16 );
		if ( hextet > 0xffff ) {
			throw new NumberFormatException();
		}
		return ( short ) hextet;
	}

	/**
	 * Parses the octet.
	 *
	 * @param ipPart the ip part
	 * @return the byte
	 */
	private static byte parseOctet( String ipPart ) {
		// Note: we already verified that this string contains only hex digits.
		int octet = Integer.parseInt( ipPart );
		// Disallow leading zeroes, because no clear standard exists on
		// whether these should be interpreted as decimal or octal.
		if ( octet > 255 || ( ipPart.startsWith( "0" ) && ipPart.length() > 1 ) ) {
			throw new NumberFormatException();
		}
		return ( byte ) octet;
	}
	
	/**
	 * Text to numeric format v4.
	 *
	 * @param ipString the ip string
	 * @return the byte[]
	 */
	private static byte[] textToNumericFormatV4( String ipString ) {
		String[] address = ipString.split( "\\.", IPV4_PART_COUNT + 1 );
		if ( address.length != IPV4_PART_COUNT ) {
			return null;
		}

		byte[] bytes = new byte[IPV4_PART_COUNT];
		try {
			for ( int i = 0; i < bytes.length; i++ ) {
				bytes[i] = parseOctet( address[i] );
			}
		} catch ( NumberFormatException ex ) {
			return null;
		}

		return bytes;
	}
	
	/**
	 * Text to numeric format v6.
	 *
	 * @param ipString the ip string
	 * @return the byte[]
	 */
	private static byte[] textToNumericFormatV6( String ipString ) {
		// An address can have [2..8] colons, and N colons make N+1 parts.
		String[] parts = ipString.split( ":", IPV6_PART_COUNT + 2 );
		if ( parts.length < 3 || parts.length > IPV6_PART_COUNT + 1 ) {
			return null;
		}

		// Disregarding the endpoints, find "::" with nothing in between.
		// This indicates that a run of zeroes has been skipped.
		int skipIndex = -1;
		for ( int i = 1; i < parts.length - 1; i++ ) {
			if ( parts[i].length() == 0 ) {
				if ( skipIndex >= 0 ) {
					return null; // Can't have more than one ::
				}
				skipIndex = i;
			}
		}

		int partsHi; // Number of parts to copy from above/before the "::"
		int partsLo; // Number of parts to copy from below/after the "::"
		if ( skipIndex >= 0 ) {
			// If we found a "::", then check if it also covers the endpoints.
			partsHi = skipIndex;
			partsLo = parts.length - skipIndex - 1;
			if ( parts[0].length() == 0 && --partsHi != 0 ) {
				return null; // ^: requires ^::
			}
			if ( parts[parts.length - 1].length() == 0 && --partsLo != 0 ) {
				return null; // :$ requires ::$
			}
		} else {
			// Otherwise, allocate the entire address to partsHi. The endpoints
			// could still be empty, but parseHextet() will check for that.
			partsHi = parts.length;
			partsLo = 0;
		}

		// If we found a ::, then we must have skipped at least one part.
		// Otherwise, we must have exactly the right number of parts.
		int partsSkipped = IPV6_PART_COUNT - ( partsHi + partsLo );
		if ( !( skipIndex >= 0 ? partsSkipped >= 1 : partsSkipped == 0 ) ) {
			return null;
		}

		// Now parse the hextets into a byte array.
		ByteBuffer rawBytes = ByteBuffer.allocate( 2 * IPV6_PART_COUNT );
		try {
			for ( int i = 0; i < partsHi; i++ ) {
				rawBytes.putShort( parseHextet( parts[i] ) );
			}
			for ( int i = 0; i < partsSkipped; i++ ) {
				rawBytes.putShort( ( short ) 0 );
			}
			for ( int i = partsLo; i > 0; i-- ) {
				rawBytes.putShort( parseHextet( parts[parts.length - i] ) );
			}
		} catch ( NumberFormatException ex ) {
			return null;
		}
		return rawBytes.array();
	}

	public static byte[] convertIPStringTo15ByteArr(String upgradeServerIp) {
		
		 byte[] bytes = InetAddressesHelper.ipStringToBytes(upgradeServerIp);
         byte[] finalBytesArr = new byte[15];
         
         for (int i = 0; i < finalBytesArr.length; i++) 
        	 finalBytesArr[i] =0;
         
         for (int i = 0; i < bytes.length; i++) 
        	 finalBytesArr[i] = bytes[i];
         
         return finalBytesArr;
	}
	
	public static String toDecimalIp(String ip, int fromBase) {
		String response = "";
		String regex = ip.contains(":") ? ":" : ip.contains(".") ? "\\." : null;
		
		if(regex == null) {
			GeneralUtils.printToConsole(ip + " cannot convert to decimal Ip");
			return null;
		}
		
		for (String temp : ip.split(regex)) {
			response += ".";
			Integer decimal = Integer.parseInt(temp, fromBase);
			response += Integer.toString(decimal);
		}

		return response.substring(1);
	}
	
	public static String changeIP(String ipAddress, int index, String newValue) {
		String[] splittedIp = ipAddress.split("\\.");
		splittedIp[index] = newValue;
		String out = "";
		for (int i = 0; i < splittedIp.length; i++) {
			if (i != 0)
				out += ".";
			out += splittedIp[i];
		}
		return out;
	}
}
