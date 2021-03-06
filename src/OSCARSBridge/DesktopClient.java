/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package OSCARSBridge;

import net.es.oscars.notify.ws.AAAFaultMessage;
import org.apache.axis2.AxisFault;
import net.es.oscars.client.*;
import net.es.oscars.wsdlTypes.*;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import net.es.oscars.oscars.*;
import org.ogf.schema.network.topology.ctrlplane.*;

/**
 *
 * @author pfbiasuz
 */
public class DesktopClient {
    
    public static String repoDir = "/home/fanesello/Documents/HYMAN/Certificados/repo";

    public ArrayList<String> createReservation(String oscars_url, String description,
            String srcUrn, String isSrcTagged, String srcTag,
            String destUrn, String isDestTagged, String destTag,
            String path, int bandwidth, String pathSetupMode,
            long startTimestamp, long endTimestamp) {

        String repo = repoDir;
        ArrayList<String> arrayToReturn = new ArrayList();
        String message;

        /**
         * pathSetupMode (string)
        "timer-automatic" means that the reserved circuit will be instantiated by the scheduler process
        "signal-xml" means the user will signal to instantiate the reserved circuit
         * aceita qualquer string no path setup mode
         * timer-automatic dispara a reserva na hora certa
         * strings invalidas nao disparam, ficam em pending como se fosse signal-xml, nao gera TOKEN
         */
        Client oscarsClient = new Client();

        /**
         * ResCreateContent
         *      PathInfo
         *          layer2Info
         *
         */
        Layer2Info layer2Info = new Layer2Info();
        layer2Info.setSrcEndpoint(srcUrn);
        layer2Info.setDestEndpoint(destUrn);

        PathInfo pathInfo = new PathInfo();

        pathInfo.setPathSetupMode(pathSetupMode);
        Boolean setPath = true;

        ResCreateContent request = new ResCreateContent();
        request.setBandwidth(bandwidth);
        request.setStartTime(startTimestamp);
        request.setEndTime(endTimestamp);
        request.setDescription(description);
        request.setPathInfo(pathInfo);

        VlanTag srcVtag = new VlanTag();

        if (isSrcTagged.equals("true")) {
            srcVtag.setTagged(true);
            srcVtag.setString(srcTag);
        } else {
            srcVtag.setTagged(false);
            srcVtag.setString("0");
        }
        layer2Info.setSrcVtag(srcVtag);

        VlanTag destVtag = new VlanTag();

        if (isDestTagged.equals("true")) {
            destVtag.setTagged(true);
            destVtag.setString(destTag);
        } else {
            destVtag.setTagged(false);
            destVtag.setString("0");
        }
        layer2Info.setDestVtag(destVtag);

        pathInfo.setLayer2Info(layer2Info);


        try {
            oscarsClient.setUp(true, oscars_url, repo);
        } catch (AxisFault e) {
            e.printStackTrace();
            message = e.getMessage();
            arrayToReturn.add(0, "Error: AxisFault (" + message + ")");
            return arrayToReturn;
        }

        try {
            CreateReply response = oscarsClient.createReservation(request);
            String gri = response.getGlobalReservationId();
            String status = response.getStatus();

            System.out.println("GRI: " + gri);
            System.out.println("Initial Status: " + status);


            arrayToReturn.add(0, "ok");
            arrayToReturn.add(gri);
            arrayToReturn.add(status);

        } catch (RemoteException e) {
            e.printStackTrace();
            message = e.getMessage();
            arrayToReturn.add(0, "Error: RemoteException (" + message + ")");
        } catch (AAAFaultMessage e) {
            e.printStackTrace();
            message = e.getMessage();
            arrayToReturn.add(0, "Error: AAAFaultMessage (" + message + ")");
        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
            arrayToReturn.add(0, "Error: Exception (" + message + ")");
        }

        return arrayToReturn;
    }

    public String[] cancel(String[] gris) {
        String url = gris[0];
        String repo = "repo";

        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, url, repo);
        } catch (AxisFault e) {
            System.out.println("AxisFault from Cancel Reservations");
            e.printStackTrace();
            String[] ret = new String[1];
            ret[0] = "Error: AxisFault from Cancel Reservations";
            return ret;
        }
        String[] status = new String[gris.length - 1];
        GlobalReservationId rt = new GlobalReservationId();
        for (int ind = 1; ind < gris.length; ind++) {
            rt.setGri(gris[ind]);
            try {
                String response = oscarsClient.cancelReservation(rt);
                status[ind - 1] = "CANCELLED";
            } catch (RemoteException e) {
                System.out.println("RemoteException from Cancel Reservations");
                e.printStackTrace();
                status[ind - 1] = "Error: RemoteException from Cancel Reservations";
            } catch (AAAFaultMessage e) {
                e.printStackTrace();
                status[ind - 1] = "Error: AAAFaultMessage from Cancel Reservations";
            } catch (Exception e) {
                e.printStackTrace();
                status[ind - 1] = "Error: Exception from Cancel Reservations";
            }
        }
        oscarsClient.cleanUp();
        return status;
    }

    public String[] list(String[] gris) {
        String url = gris[0];
        String repo = "repo";
        System.out.println("Exec DIR: " + System.getProperty("user.dir"));

        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, url, repo);
        } catch (AxisFault e) {
            System.out.println("AxisFault from List Reservations");
            e.printStackTrace();
            String[] ret = new String[1];
            ret[0] = "Error: AxisFault from List Reservations";
            return ret;
        }
        String[] status = new String[gris.length - 1];
        ResDetails response = new ResDetails();
        GlobalReservationId rt = new GlobalReservationId();
        for (int ind = 1; ind < gris.length; ind++) {
            rt.setGri(gris[ind]);
            try {
                response = oscarsClient.queryReservation(rt);
                status[ind - 1] = response.getStatus();
            } catch (RemoteException e) {
                System.out.println("RemoteException from List Reservations");
                status[ind - 1] = "Error: RemoteException from List Reservations";
            } catch (AAAFaultMessage e) {
                status[ind - 1] = "Error: AAAFaultMessage from List Reservations";
            } catch (Exception e) {
                status[ind - 1] = "Error: Exception from List Reservations";
            }

        }
        oscarsClient.cleanUp();
        return status;
    }

    public String[] query(String[] gris) {
        String url = gris[0];
        String repo = "repo";
        System.out.println("Exec DIR: " + System.getProperty("user.dir"));

        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, url, repo);
        } catch (AxisFault e) {
            System.out.println("AxisFault from Query Reservations");
            String[] ret = new String[1];
            ret[0] = "Error: AxisFault from Query Reservations";
            return ret;
        }
        String[] info = new String[(gris.length - 1) * 3]; //inicio, fim, status,
        ResDetails response = new ResDetails();
        GlobalReservationId rt = new GlobalReservationId();
        int retInd = 0;
        for (int ind = 1; ind < gris.length; ind++) {
            rt.setGri(gris[ind]);
            try {
                response = oscarsClient.queryReservation(rt);
                info[retInd++] = response.getStatus();
                info[retInd++] = String.valueOf(response.getStartTime());
                info[retInd++] = String.valueOf(response.getEndTime());
            } catch (RemoteException e) {
                System.out.println("RemoteException from Query Reservation");
                info[retInd++] = "Error: RemoteException from Query Reservations";
                info[retInd++] = "Error: RemoteException from Query Reservations";
                info[retInd++] = "Error: RemoteException from Query Reservations";
            } catch (AAAFaultMessage e) {
                info[retInd++] = "Error: AAAFaultMessage from Query Reservations";
                info[retInd++] = "Error: AAAFaultMessage from Query Reservations";
                info[retInd++] = "Error: AAAFaultMessage from Query Reservations";
            } catch (Exception e) {
                info[retInd++] = "Error: Exception from Query Reservations";
                info[retInd++] = "Error: Exception from Query Reservations";
                info[retInd++] = "Error: Exception from Query Reservations";
            }
        }
        oscarsClient.cleanUp();
        return info;
    }

    public void queryAll(String gri) {
        String url = "http://200.132.1.28:8085/axis2/services/OSCARS";
        String oscars_url = "http://oscars.cipo.rnp.br:8080/axis2/services/OSCARS";
        String repo = repoDir;

        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, oscars_url, repo);
        } catch (AxisFault e) {
            System.out.println("AxisFault from Query Reservations");
            String[] ret = new String[1];
            ret[0] = "Error: AxisFault from Query Reservations";
            System.out.println(ret[0]);
        }
        try {
            GlobalReservationId request = new GlobalReservationId();
            request.setGri(gri);
            /* Send Request */
            ResDetails response = oscarsClient.queryReservation(request);
            PathInfo pathInfo = response.getPathInfo();
            CtrlPlanePathContent path = pathInfo.getPath();
            Layer2Info layer2Info = pathInfo.getLayer2Info();
            Layer3Info layer3Info = pathInfo.getLayer3Info();
            MplsInfo mplsInfo = pathInfo.getMplsInfo();

            /* Print repsponse information */
            System.out.println("GRI: " + response.getGlobalReservationId());
            System.out.println("Login: " + response.getLogin());
            System.out.println("Status: " + response.getStatus());
            System.out.println("Start Time: " + response.getStartTime());
            System.out.println("End Time: " + response.getEndTime());
            System.out.println("Time of request: " + response.getCreateTime());
            System.out.println("Bandwidth: " + response.getBandwidth());
            System.out.println("Description: " + response.getDescription());
            System.out.println("Path Setup Mode: " + pathInfo.getPathSetupMode());
            if (layer2Info != null) {
                System.out.println("Source Endpoint: " + layer2Info.getSrcEndpoint());
                VlanTag srcVtag = new VlanTag();
                srcVtag = layer2Info.getSrcVtag();
                String srcVlan = srcVtag.getString();
                Boolean isTagged = srcVtag.getTagged();
                System.out.println("Is tagged: " + isTagged.toString());
                System.out.println("Vlan value: " + srcVlan);
                System.out.println("Destination Endpoint: " + layer2Info.getDestEndpoint());
                VlanTag dstVtag = new VlanTag();
                dstVtag = layer2Info.getDestVtag();
                String dstVlan = dstVtag.getString();
                Boolean isTaggedDest = dstVtag.getTagged();
                System.out.println("Is tagged: " + isTaggedDest.toString());
                System.out.println("Vlan value: " + dstVlan);
            }
            if (layer3Info != null) {
                System.out.println("Source Host: " + layer3Info.getSrcHost());
                System.out.println("Destination Host: " + layer3Info.getDestHost());
                System.out.println("Source L4 Port: " + layer3Info.getSrcIpPort());
                System.out.println("Destination L4 Port: " + layer3Info.getDestIpPort());
                System.out.println("Protocol: " + layer3Info.getProtocol());
                System.out.println("DSCP: " + layer3Info.getDscp());
            }
            if (mplsInfo != null) {
                System.out.println("Burst Limit: " + mplsInfo.getBurstLimit());
                System.out.println("LSP Class: " + mplsInfo.getLspClass());
            }
            System.out.println("Path: ");
            String output = "";
            for (CtrlPlaneHopContent hop : path.getHop()) {
                CtrlPlaneLinkContent link = hop.getLink();
                if (link == null) {
                    //should not happen
                    output += "no link";
                    continue;
                }
                output += "\t" + link.getId();
                CtrlPlaneSwcapContent swcap = link.getSwitchingCapabilityDescriptors();
                CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo = swcap.getSwitchingCapabilitySpecificInfo();
                output += ", " + swcap.getEncodingType();
                if ("ethernet".equals(swcap.getEncodingType())) {
                    output += ", " + swcapInfo.getVlanRangeAvailability();
                }
                output += "\n";
            }
            System.out.println(output);
        } catch (AxisFault e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AAAFaultMessage e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

  /**
         * Calls an IDC to get a global view of the domain's topology.
         * Request message parameters:
         * topologyType (string) - currently must have the value "all". In the future the values:
         * "adjacentdomains","delta" or "internetworklinks" will be supported as well.
         * esses valores funcionam como ALL - nao funcionam adequadamente
         */
    public void getTopology(String oscars_url) {

        String url = "http://200.132.1.28:8080/axis2/services/OSCARS";
        String repo = repoDir;
        String temp;
        
        Client oscarsClient = new Client();
        try {
            //oscarsClient.setUp(true, url, repo);
            oscarsClient.setUp(true, oscars_url, repo);
        } catch (AxisFault e) {
            e.printStackTrace();
        }

        GetTopologyContent request = new GetTopologyContent();
        request.setTopologyType("all");
        try {
            GetTopologyResponseContent response = oscarsClient.getNetworkTopology(request);

            CtrlPlaneDomainContent[] domains = response.getTopology().getDomain();
            /* Output topology in response */
            for (CtrlPlaneDomainContent d : domains) {
                temp = d.getId();
                System.out.println(temp);
                CtrlPlaneNodeContent[] nodes = d.getNode();
                for (CtrlPlaneNodeContent n : nodes) {
                    temp = n.getId();
                    System.out.println(temp);
                    CtrlPlanePortContent[] ports = n.getPort();
                    for (CtrlPlanePortContent p : ports) {
                        temp = p.getId()+" " +p.getCapacity()+ " "+p.getGranularity()+ " "+p.getMinimumReservableCapacity()+" "+p.getMaximumReservableCapacity();
                        System.out.println(temp);
                        CtrlPlaneLinkContent[] links = p.getLink();
                        if (links != null) {
                            for (CtrlPlaneLinkContent l : links) {
                                CtrlPlaneSwcapContent swcap = l.getSwitchingCapabilityDescriptors();
                                CtrlPlaneSwitchingCapabilitySpecificInfo swcapEsp = swcap.getSwitchingCapabilitySpecificInfo();
                                temp = l.getId()+" "+l.getRemoteLinkId()+" "+l.getCapacity()+ " "+l.getGranularity()+ " "+l.getMinimumReservableCapacity()+" "+l.getMaximumReservableCapacity()+" "+swcapEsp.getVlanRangeAvailability();
                                System.out.println(temp);
                            }
                        }
                    }
                }
            }
            System.out.println(response);
        } catch (AAAFaultMessage e) {
            System.out.println("AAA Error: " + e.getMessage());
        } catch (BSSFaultMessage e) {
            System.out.println("BSS Error: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Remote Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void listAllReservations(String oscars_url, String status) {
        String repo = repoDir;

        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, oscars_url, repo);
        } catch (AxisFault e) {
            System.out.println("AxisFault: " + e.getMessage());
        }
        try {
            ListRequest request = new ListRequest();
            request.addResStatus(status);
            ListReply reply = oscarsClient.listReservations(request);
            ResDetails[] details = reply.getResDetails();
            int numFilteredResults = 0;
            for (ResDetails detail : details) {
                PathInfo pathInfo = detail.getPathInfo();
                CtrlPlanePathContent path = pathInfo.getPath();
                Layer2Info layer2Info = pathInfo.getLayer2Info();
                Layer3Info layer3Info = pathInfo.getLayer3Info();
                MplsInfo mplsInfo = pathInfo.getMplsInfo();

                String output = "";
                output += "GRI: " + detail.getGlobalReservationId() + "\n";
                output += "Start Time: " + detail.getStartTime() + "\n";
                output += "End Time: " + detail.getEndTime() + "\n";
                output += "Bandwidth: " + detail.getBandwidth() + "\n";
                output += "Description: " + detail.getDescription() + "\n";
                if(layer2Info != null){
                    output += "Source Endpoint: " + layer2Info.getSrcEndpoint() + "\n";
                    output += "Destination Endpoint: " + layer2Info.getDestEndpoint() + "\n";
                    output += "Source VLAN: " + layer2Info.getSrcVtag() + "\n";
                    output += "Destination VLAN: " + layer2Info.getDestVtag() + "\n";
                }
                if(layer3Info != null){
                    output += "Source Host: " + layer3Info.getSrcHost() + "\n";
                    output += "Destination Host: " + layer3Info.getDestHost() + "\n";
                    output += "Source L4 Port: " + layer3Info.getSrcIpPort() + "\n";
                    output += "Destination L4 Port: " + layer3Info.getDestIpPort() + "\n";
                }
                if(mplsInfo != null){
                    output += "Burst Limit: " + mplsInfo.getBurstLimit() + "\n";
                    output += "LSP Class: " + mplsInfo.getLspClass() + "\n";
                }
                System.out.println(output);
            }
        } catch (AxisFault e) {
            System.out.println("AxisFault: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Remote Exception: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    public void createTeste() {
        String url = "http://200.132.1.28:8085/axis2/services/OSCARS";
        String repo = "repo";

        /**
         * pathSetupMode (string)
        "timer-automatic" means that the reserved circuit will be instantiated by the scheduler process
        "signal-xml" means the user will signal to instantiate the reserved circuit
         * aceita qualquer string no path setup mode
         * timer-automatic dispara a reserva na hora certa
         * strings invalidas nao disparam, ficam em pending como se fosse signal-xml, nao gera TOKEN
         */
        Client oscarsClient = new Client();

        /**
         * ResCreateContent
         *      PathInfo
         *          layer2Info
         *
         */
        Layer2Info layer2Info = new Layer2Info();
        layer2Info.setSrcEndpoint("urn:ogf:network:domain=oscars5.ufrgs.br:node=vlsr1:port=3:link=11.2.1.2");
        layer2Info.setDestEndpoint("urn:ogf:network:domain=oscars7.ufrgs.br:node=vlsr1:port=3:link=11.3.9.1");

        PathInfo pathInfo = new PathInfo();

        pathInfo.setPathSetupMode("timer-automatic");
        Boolean setPath = false;

        if (setPath) {

            /**
             *  pontos origem e destino devem ser colocados completos nos hops, se o ponto de início/fim for algum hop antes, ele configura somente até o hop discriminado
             *  pontos intermediários podem ser definidos parcialmente
             *
             */
            String[] hops = {
"urn:ogf:network:domain=oscars5.ufrgs.br:node=vlsr1:port=3:link=11.2.1.2",
"urn:ogf:network:domain=oscars5.ufrgs.br:node=vlsr1:port=5:link=11.2.3.1",
"urn:ogf:network:domain=oscars5.ufrgs.br:node=vlsr3:port=5:link=11.2.3.2",
"urn:ogf:network:domain=oscars5.ufrgs.br:node=vlsr3:port=6:link=11.2.12.1",
"urn:ogf:network:domain=oscars7.ufrgs.br:node=vlsr2:port=5:link=11.2.12.2",
"urn:ogf:network:domain=oscars7.ufrgs.br:node=vlsr2:port=4:link=11.3.11.1",
"urn:ogf:network:domain=oscars7.ufrgs.br:node=vlsr1:port=4:link=11.3.11.2",
"urn:ogf:network:domain=oscars7.ufrgs.br:node=vlsr1:port=3:link=11.3.9.1"
            };



            CtrlPlanePathContent path = new CtrlPlanePathContent();
            path.setId("userPath");
            boolean hasEro = true;

            for (int i = 0; i < hops.length; i++) {
                String propName = "ero_" + Integer.toString(i);
                String hopId = hops[i];
                if (hopId != null) {
                    hopId = hopId.trim();
                    int hopType = hopId.split(":").length;
                    hasEro = true;
                    CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
                    hop.setId(i + "");
                    if (hopType == 4) {
                        hop.setDomainIdRef(hopId);
                    } else if (hopType == 5) {
                        hop.setNodeIdRef(hopId);
                    } else if (hopType == 6) {
                        hop.setPortIdRef(hopId);
                    } else {
                        hop.setLinkIdRef(hopId);
                    }
                    path.addHop(hop);
                }
            }
            if (hasEro) {
                pathInfo.setPath(path);
                //pathInfo.setPathType("0");
            }
        }

        ResCreateContent request = new ResCreateContent();
        request.setBandwidth(1000);
        request.setStartTime(1323370800);
        request.setEndTime(1323374400);
        request.setDescription("testes full path");
        request.setPathInfo(pathInfo);

        /**
         * nao consegue setar vlans diferentes entre origem e destino. Resulta em FAILED. Na wbui tambem nao.
         * Setando a vlan origem, a vlan destino é a mesma (normalmente)
         *
         */
        String vlanValue = "";

        String vlanSrc = vlanValue;

       if (!vlanValue.equals("")) {
            VlanTag srcVtag = new VlanTag();
            srcVtag.setString(vlanSrc.trim());
            srcVtag.setTagged(false);
            layer2Info.setSrcVtag(srcVtag);
        }

        String vlanDest = vlanValue;

        if (!vlanValue.equals("")) {
            VlanTag destVtag = new VlanTag();
            destVtag.setString(vlanDest.trim());
            destVtag.setTagged(false);
            layer2Info.setDestVtag(destVtag);
        }
        pathInfo.setLayer2Info(layer2Info);


        try {
            oscarsClient.setUp(true, url, repo);
        } catch (AxisFault e) {
            System.out.println("AxisFault from Create Reservations");
            e.printStackTrace();
        }

        try {
            CreateReply response = oscarsClient.createReservation(request);
            String gri = response.getGlobalReservationId();
            String status = response.getStatus();
            String token = response.getToken();

            System.out.println("GRI: " + gri);
            System.out.println("Initial Status: " + status);
            System.out.println("Token: " + token);

        } catch (RemoteException e) {
            System.out.println("RemoteException from Create Reservations");
            e.printStackTrace();

        } catch (AAAFaultMessage e) {
            System.out.println("AAAFaultMessage from Create Reservations");
            e.printStackTrace();


        } catch (Exception e) {
            System.out.println("Exception from Create Reservations");
            e.printStackTrace();

        }
    }


            /**
         * demora um certo tempo para cancelar
         * o cancelar utilizando os clientes java é mais rapido de ser efetuado
         */
    public void cancelTeste(String gri) {

        String url = "http://200.132.1.28:8087/axis2/services/OSCARS";
        String repo = "repo";

        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, url, repo);
        } catch (AxisFault e) {
            e.printStackTrace();
        }

        GlobalReservationId rt = new GlobalReservationId();

        rt.setGri(gri);
        try {
            String response = oscarsClient.cancelReservation(rt);

        } catch (RemoteException e) {
            e.printStackTrace();

        } catch (AAAFaultMessage e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /**
     *  unica coisa que muda é o tempo de inicio e fim.
     *  o resto nao é possível alterar
     * @param gri
     */
    public void modifyReservation(String gri) {
        String url = "http://200.132.1.28:8087/axis2/services/OSCARS";
        String repo = "repo";

        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, url, repo);
        } catch (AxisFault e) {
            e.printStackTrace();
        }

        ModifyResContent content = new ModifyResContent();

       // int bandwidth = 100;
        long start = (System.currentTimeMillis() / 1000 + 60 * 60);
        long end = (System.currentTimeMillis() / 1000 + 60 * 120);
        content.setBandwidth(100); //PRECISA ESTAR SETADO E NAO ALTERA DE JEITO NENHUM
        content.setGlobalReservationId(gri);
        content.setDescription("modify3"); //PRECISA ESTAR SETADO MAS NAO ALTERA DE JEITO NENHUM
        content.setStartTime(start); //PRECISA ESTAR SETADO SEMPRE ALTERA, SE NAO DEFINIDO VAI PARA 1969
        content.setEndTime(end);
        
        Layer2Info layer2Info = new Layer2Info();
        VlanTag srcVtag = new VlanTag();
        VlanTag destVtag = new VlanTag();
        PathInfo pathInfo = new PathInfo();
        
        srcVtag.setString("192");
            srcVtag.setTagged(true);
            layer2Info.setSrcVtag(srcVtag);
            
            // same as srcVtag for now
            destVtag.setString("192");
            destVtag.setTagged(true);
            layer2Info.setDestVtag(destVtag);
            layer2Info.setSrcEndpoint("urn:ogf:network:domain=oscars7.ufrgs.br:node=vlsr1:port=3:link=11.3.9.1");
             layer2Info.setDestEndpoint("urn:ogf:network:domain=oscars2.ufrgs.br:node=vlsr1:port=3:link=11.1.8.1");
            
        pathInfo.setPathSetupMode("timer-automatic");
        
            pathInfo.setLayer2Info(layer2Info);
            content.setPathInfo(pathInfo);



        try {
            ModifyResReply response = oscarsClient.modifyReservation(content);
            ResDetails reservation = response.getReservation();


            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            date.setTime(reservation.getStartTime() * 1000L);
            String startTime = df.format(date);

            date.setTime(reservation.getEndTime() * 1000L);
            String endTime = df.format(date);

            System.out.println("\n\nResponse:\n");
            System.out.println("GRI: " + reservation.getGlobalReservationId());
            System.out.println("Status: " + reservation.getStatus().toString()); //INMODIFY
            //NAO RETORNA OS NOVOS VALORES
            System.out.println("New startTime: " + startTime);
            System.out.println("New endTime: " + endTime);


        } catch (RemoteException e) {
            e.printStackTrace();

        } catch (AAAFaultMessage e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    /**
     * Causes all the IDCs on the path to provision the path for a previously scheduled reservation
     * whose start time has been reached. The reservation must have been created with a
     * pathSetupMode of "xml-signal".
     * @param gri
     */
    public void createPath(String gri) {
        String url = "http://200.132.1.28:8087/axis2/services/OSCARS";
        String repo = "repo";

        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, url, repo);
        } catch (AxisFault e) {
            e.printStackTrace();
        }

        CreatePathContent createRequest = new CreatePathContent();
        createRequest.setGlobalReservationId(gri);
        try {
            CreatePathResponseContent createResponse = oscarsClient.createPath(createRequest);
            System.out.println("Global Reservation Id: " + createResponse.getGlobalReservationId());
            System.out.println("Create Status: " + createResponse.getStatus());
        } catch (RemoteException e) {
            e.printStackTrace();

        } catch (AAAFaultMessage e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  Checks to see if the path of a scheduled reservation is still valid. Checks local path  first
     *  and if it is ok  then forwards the refreshPath request. If the local path has failed it forwards
     *  a teardown message. If the forwardResponse indicates an downstream error the local path is
     *  removed and the exception passed upstream.
     *
     * throws axisfault in non-active reservations
     */
    public void refreshPath(String gri) {

        String url = "http://200.132.1.28:8087/axis2/services/OSCARS";
        String repo = "repo";

        RefreshPathContent refreshRequest = new RefreshPathContent();
        refreshRequest.setGlobalReservationId(gri);

        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, url, repo);
        } catch (AxisFault e) {
            e.printStackTrace();
        }

        try {
            RefreshPathResponseContent refreshResponse = oscarsClient.refreshPath(refreshRequest);
            System.out.println("Global Reservation Id: " + refreshResponse.getGlobalReservationId());
            System.out.println("Refresh Status: " + refreshResponse.getStatus());
        } catch (RemoteException e) {
            e.printStackTrace();

        } catch (AAAFaultMessage e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  Will teardown an ACTIVE reservation. Removes local path  first and then forwards request.
     *  If there is a failure in the local path the teardown the request is still forwarded and the
     *  exception is reported  upstream. The reservation status is reset to PENDING  if the reservation
     *  end time has not been reached, so that the path could still rebuilt.
     *
     *  Caso seja feito uma operação TEARDOWN sobre uma reserva que não esteja ativa causa a FAILED na reserva.
     *  Reservas com "timer-automatic" não é possível utilizar a operação TEARDOWN. Causa a FAILED sobre a reserva timer-automatic.
     */
    public void teardownPath(String gri) {
        String url = "http://200.132.1.28:8087/axis2/services/OSCARS";
        String repo = "repo";

        TeardownPathContent teardownRequest = new TeardownPathContent();
        teardownRequest.setGlobalReservationId(gri);

        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, url, repo);
        } catch (AxisFault e) {
            e.printStackTrace();
        }

        try {
            TeardownPathResponseContent teardownResponse = oscarsClient.teardownPath(teardownRequest);
            System.out.println("Global Reservation Id: " + teardownResponse.getGlobalReservationId());
            System.out.println("Teardown Status: " + teardownResponse.getStatus());
        } catch (RemoteException e) {
            e.printStackTrace();

        } catch (AAAFaultMessage e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 1. cria um cliente que busca a topologia entre dois pontos quaisquer. Dispara a reserva ao OSCARS (se autorizada) como timer-automatic
     * 2. busca a topologia inteira de todos os domínios que fazem parte do domínio MEICAN e constrói no MEICAN um reflexo da topologia. Calculo de rotas sem passar pelo OSCARS.
     *        Para garantir a equivalência das rotas calculadas entre o MEICAN  e OSCARS pode-se forçar o caminho com o pathType=1 (precisa testes) no momento de criar a reserva no OSCARS (como timer-automatic)
     * 3. cria uma reserva signal-xml para obter a topologia  e desenvolve um daemon para no momento de efetuar a reserva (se autorizada) disparar o createPath
     *      Se chegar momento da reserva sem autorização ou for negada, deve-se cancelar solicitação.
     *
     */
}
