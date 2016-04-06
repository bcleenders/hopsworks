package se.kth.hopsworks.rest;


import org.apache.commons.beanutils.BeanUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.json.JSONObject;
import se.kth.hopsworks.controller.ResponseMessages;
import se.kth.hopsworks.filters.AllowedRoles;
import se.kth.hopsworks.workflows.Node;
import se.kth.hopsworks.workflows.NodeFacade;
import se.kth.hopsworks.workflows.NodePK;
import se.kth.hopsworks.workflows.Workflow;

import javax.ejb.EJB;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.RequestScoped;
import javax.json.Json;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import se.kth.hopsworks.workflows.nodes.*;
import static com.google.common.base.CaseFormat.*;

@RequestScoped
@TransactionAttribute(TransactionAttributeType.NEVER)
public class NodeService {
    private final static Logger logger = Logger.getLogger(NodeService.class.
            getName());


    @EJB
    private NodeFacade nodeFacade;

    @EJB
    private NoCacheResponse noCacheResponse;

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    private Workflow workflow;

    public NodeService(){

    }

//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    @AllowedRoles(roles = {AllowedRoles.DATA_SCIENTIST, AllowedRoles.DATA_OWNER})
//    public Response index() throws AppException {
//        List<Node> nodes = nodeFacade.findAll();
//        return noCacheResponse.getNoCacheResponseBuilder(Response.Status.OK).entity(nodes).build();
//    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @AllowedRoles(roles = {AllowedRoles.DATA_SCIENTIST, AllowedRoles.DATA_OWNER})
    public Response create(
            String stringParams,
            @Context HttpServletRequest req) throws AppException, ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode params = mapper.readTree(stringParams);
        Map<String, Object> paramsMap = mapper.convertValue(params, Map.class);

        String className = LOWER_HYPHEN.to(UPPER_CAMEL, paramsMap.get("type").toString());
        Class nodeClass = Class.forName("se.kth.hopsworks.workflows.nodes." + className);
        Node node = (Node)nodeClass.newInstance();

        paramsMap.put("data", params.get("data"));
//        paramsMap.put("data", new JSONObject(stringParams).getJSONObject("data"));
        BeanUtils.populate(node, paramsMap);
        node.setWorkflowId(workflow.getId());


        nodeFacade.persist(node);
        JsonNode json = new ObjectMapper().valueToTree(node);
        return noCacheResponse.getNoCacheResponseBuilder(Response.Status.OK).entity(json.toString()).build();

    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @AllowedRoles(roles = {AllowedRoles.DATA_SCIENTIST, AllowedRoles.DATA_OWNER})
    public Response show(
            @PathParam("id") String id) throws AppException {
        NodePK nodePk = new NodePK(id, workflow.getId());

        Node node = nodeFacade.findById(nodePk);
        if (node == null) {
            throw new AppException(Response.Status.BAD_REQUEST.getStatusCode(),
                    ResponseMessages.NODE_NOT_FOUND);
        }
        JsonNode json = new ObjectMapper().valueToTree(node);
        return noCacheResponse.getNoCacheResponseBuilder(Response.Status.OK).entity(json.toString()).build();
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @AllowedRoles(roles = {AllowedRoles.DATA_SCIENTIST, AllowedRoles.DATA_OWNER})
    public Response update(
            String stringParams,
            @PathParam("id") String id
    ) throws AppException, IOException, IllegalAccessException, InvocationTargetException {

        NodePK nodePk = new NodePK(id, workflow.getId());
        Node node = nodeFacade.findById(nodePk);
        if (node == null) {
            throw new AppException(Response.Status.BAD_REQUEST.getStatusCode(),
                    ResponseMessages.NODE_NOT_FOUND);
        }
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode params = (ObjectNode) mapper.readTree(stringParams);
        Map<String, Object> paramsMap = mapper.convertValue(params, Map.class);

        String className = LOWER_HYPHEN.to(UPPER_CAMEL, params.get("type").getValueAsText());
        paramsMap.put("classname", className);
        paramsMap.put("data", params.get("data"));
        BeanUtils.populate(node, paramsMap);
        nodeFacade.merge(node);

        JsonNode json = new ObjectMapper().valueToTree(nodeFacade.refresh(node));
        return noCacheResponse.getNoCacheResponseBuilder(Response.Status.OK).entity(json.toString()).build();
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @AllowedRoles(roles = {AllowedRoles.DATA_SCIENTIST, AllowedRoles.DATA_OWNER})
    public Response delete(
            @PathParam("id") String id) throws AppException {
        NodePK nodePk = new NodePK(id, workflow.getId());
        Node node = nodeFacade.findById(nodePk);
        if (node == null) {
            throw new AppException(Response.Status.BAD_REQUEST.getStatusCode(),
                    ResponseMessages.NODE_NOT_FOUND);
        }
        nodeFacade.remove(node);
        return noCacheResponse.getNoCacheResponseBuilder(Response.Status.OK).build();
    }
}
