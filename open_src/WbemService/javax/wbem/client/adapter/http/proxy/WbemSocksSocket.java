package javax.wbem.client.adapter.http.proxy;

import socks.Proxy;
import socks.SocksException;
import socks.SocksSocket;

public class WbemSocksSocket extends SocksSocket 
{
	
	public WbemSocksSocket(Proxy p) throws SocksException
	{
		super(p.getInetAddress(), p.getPort(), p);
		this.proxy = p;
	}
}
