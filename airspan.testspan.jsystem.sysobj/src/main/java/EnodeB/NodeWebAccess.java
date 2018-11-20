package EnodeB;

import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import Utils.GeneralUtils;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * supports ip add, subnetMask, Default Gate Way, VlanTagId, ManagementVLAN from
 * Web GUI.
 *
 * @author sshahaf
 */
public class NodeWebAccess {
    private final String dataPath = "cgi-bin/lteCfgWeb.cgi";
    private final String dataSubmit = "cgi-bin/writeLteCfgWeb.cgi";
    private String ip = "defaultValue";
    private String subNetMask = "defaultValue";
    private String defaultGw = "defaultValue";
    private String managementVlan = "defaultValue";
    private String vlanTagId = "defaultValue";

    public String getParameterValue(WebGuiParameters parameter, String ip) {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("http://" + ip + "/" + dataPath).get()
                .addHeader("authorization", "Basic YWlyNGd3ZWI6ciF5IzNTNC0jcXYyPzNIQg==")
                .addHeader("cache-control", "no-cache").build();
        okhttp3.Response response = executeCall(client, request);

        if (response == null) {
            return null;
        }

        String result = parseResponseWithParameter(response, parameter);
        response.close();
        return result;
    }

    public String getSecuredParameterValue(WebGuiParameters parameter, String ip) {

        OkHttpClient client = getUnsafeOkHttpClient();
        if (client == null) {
            return null;
        }
        Request request = new Request.Builder().url("https://" + ip + "/" + dataPath).get()
                .addHeader("authorization", "Basic YWlyNGd3ZWI6ciF5IzNTNC0jcXYyPzNIQg==")
                .addHeader("cache-control", "no-cache").build();
        okhttp3.Response response = executeCall(client, request);

        if (response == null) {
            return null;
        }

        String result = parseResponseWithParameter(response, parameter);
        response.close();
        return result;
    }

    public boolean setParameterValue(WebGuiParameters parameter, String ipAdd, String value) {
        getAllParametersFromWebGuiToLocalParameters(ipAdd);
        if (!ValidateLocalParametersNotNull()) {
            return false;
        }
        setNewIncomeParameterToTheRightLocalParameter(parameter, value);
        Request request = buildSetCommandAsPostMethod(ipAdd);
        return sendSubmitRequest(request);
    }

    /**
     * return false if at least one parameter is null return true in case all
     * parameters are not null.
     */
    private boolean ValidateLocalParametersNotNull() {
        System.out.println("Checking if all parameters have local value different than null");
        return (!isNull(this.ip)) && (!isNull(this.defaultGw)) && (!isNull(this.managementVlan))
                && (!isNull(this.vlanTagId)) && (!isNull(this.subNetMask));
    }

    /**
     * true if object equals to null false if not equals to null
     */
    private boolean isNull(Object object) {
        System.out.println("object = " + object);
        return object == null;
    }

    private boolean sendSubmitRequest(Request request) {
        boolean result = false;
        OkHttpClient client = new OkHttpClient();
        okhttp3.Response response = executeCall(client, request);
        if (response == null) {
            return false;
        }
        result = checkResponseWithString(response, "changes will take effect after reboot");
        response.close();
        return result;
    }

    private boolean checkResponseWithString(Response response, String string) {
        try {
            if (response.body().string().toLowerCase().contains("changes will take effect after reboot")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private Request buildSetCommandAsPostMethod(String ipAdd) {
        RequestBody formBody = buildFormWithLocalParameters();
        Request request = new Request.Builder().url("http://" + ipAdd + "/" + dataSubmit).post(formBody)
                .addHeader("remote address", ipAdd + ":80")
                .addHeader("accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .addHeader("accept-encoding", "gzip, deflate").addHeader("accept-language", "en-US,en;q=0.8")
                .addHeader("authorization", "Basic YWlyNGd3ZWI6ciF5IzNTNC0jcXYyPzNIQg==")
                .addHeader("content-type", "application/x-www-form-urlencoded").addHeader("host", ipAdd)
                .addHeader("referer", "http://" + ipAdd + "/" + dataPath).addHeader("cache-control", "no-cache")
                .build();
        return request;
    }

    private RequestBody buildFormWithLocalParameters() {
        FormBody.Builder form = new FormBody.Builder().add("IpAddress", this.ip).add("SubnetMask", this.subNetMask)
                .add("DefaultGateway", this.defaultGw).add("ManagementVLAN", this.managementVlan)
                .add("VLANTagId", this.vlanTagId).add("ipSubmit", "Submit");
        RequestBody formBody = form.build();
        return formBody;
    }

    private boolean setNewIncomeParameterToTheRightLocalParameter(WebGuiParameters parameter, String value) {
        boolean result = true;
        switch (parameter) {
            case IP_ADDRESS:
                this.ip = value;
                break;
            case SUBNET_MASK:
                this.subNetMask = value;
                break;
            case DEFAULT_GATE_WAY:
                this.defaultGw = value;
                break;
            case MANAGEMENTVLAN:
                this.managementVlan = value;
                break;
            case VLANTagId:
                this.vlanTagId = value;
                break;
            default:
                System.out.println("Incomming parameter Value is not one of the Web GUI parameters!");
                result = false;
                break;
        }
        return result;
    }

    private void getAllParametersFromWebGuiToLocalParameters(String ip) {
        this.ip = getParameterValue(WebGuiParameters.IP_ADDRESS, ip);
        this.subNetMask = getParameterValue(WebGuiParameters.SUBNET_MASK, ip);
        this.defaultGw = getParameterValue(WebGuiParameters.DEFAULT_GATE_WAY, ip);
        this.managementVlan = getParameterValue(WebGuiParameters.MANAGEMENTVLAN, ip);
        this.vlanTagId = getParameterValue(WebGuiParameters.VLANTagId, ip);
    }

    private String parseResponseWithParameter(Response response, WebGuiParameters parameter) {
        String result = null;
        try {
            Document doc = Jsoup.parse(response.body().string());
            Elements htmlEle = doc.getElementsByTag("html");
            String paramValue = parameter.getValue();
            if (paramValue.toLowerCase().contains("management")) {
                result = optionElement(htmlEle, parameter);
            } else {
                Element htmlEle0 = htmlEle.get(0).getElementsByAttributeValue("name", paramValue).get(0);
                result = htmlEle0.val();
            }
            GeneralUtils.printToConsole("parameter (" + parameter.getValue() + ") : " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String optionElement(Elements htmlEle, WebGuiParameters param) {
        String result = null;
        switch (param) {
            case MANAGEMENTVLAN: {
                Element htmlEle0 = htmlEle.get(0).getElementsByAttributeValue("value", "TAGGED").get(0);
                if (htmlEle0.toString().contains("selected")) {
                    result = htmlEle0.val();
                }

                htmlEle0 = htmlEle.get(0).getElementsByAttributeValue("value", "UNTAGGED").get(0);
                if (htmlEle0.toString().contains("selected")) {
                    result = htmlEle0.val();
                }
            }
            default:
        }
        return result;
    }

    private Response executeCall(OkHttpClient client, Request request) {
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (response.code() != 200) {
                GeneralUtils.printToConsole("Response code is : " + response.code() + ", and not as Expected");
                response = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            GeneralUtils.printToConsole("Web Gui Error - while trying to send method to URL: " + request.url()
                    + ", returning Empty response");
            response = null;
        }
        return response;
    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }};

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            @SuppressWarnings("unused")
            OkHttpClient okHttpClient;
            return okHttpClient = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
            GeneralUtils.printToConsole("Failed At method getUnsafeOkHttpClient");
            return null;
        }
    }

}
