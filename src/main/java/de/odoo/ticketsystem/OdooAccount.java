package de.odoo.ticketsystem;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URL;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;

@RestController
public class OdooAccount {

    private final String db = "adesso1";
    private final String username = "dmytro.hutkin@adesso.de";
    private final String password = "123d321";

    @RequestMapping("")
    public Object authenticate() throws MalformedURLException {
        String url = "https://adesso1.odoo.com";
        final XmlRpcClient client = new XmlRpcClient();
        final XmlRpcClientConfigImpl common_config = new XmlRpcClientConfigImpl();
        common_config.setServerURL(new URL(String.format("%s/xmlrpc/2/common", url)));
        try {
            client.execute(
                    common_config, "authenticate", asList(
                            db, username, password, emptyMap()));
            return "Login Successful!";
        } catch (XmlRpcException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
