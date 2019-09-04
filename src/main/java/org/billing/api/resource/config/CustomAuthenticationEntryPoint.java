package org.billing.api.resource.config;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint extends BasicAuthenticationEntryPoint
{
    @Override
    public void commence
            (HttpServletRequest request, HttpServletResponse response, AuthenticationException authEx)
            throws IOException, ServletException {
        response.setContentType("application/json");
        response.addHeader("WWW-Authenticate", "Basic realm=" + getRealmName());  
        response.setStatus(HttpServletResponse.SC_OK);
        Map<String,Object> params  = new HashMap<String,Object>();
        Map<String,Object>  statusDetails = new HashMap<>();
        Map<String,Object>  exceptionDetails = new HashMap<>();
        Map<String,String>  productDetails = new HashMap<>();
        productDetails.put("name","Apirus");
        productDetails.put("version","v1.0");

        if(authEx.getMessage()!=null)
        {
            JsonNode jsonNode;
            try
            {
                jsonNode = new ObjectMapper().readTree(authEx.getMessage());
                statusDetails.put("code",jsonNode.get("statusCode"));
                //response.setStatus(Integer.parseInt(jsonNode.get("statusCode").toString()));
                statusDetails.put("value",jsonNode.get("statusValue"));
                exceptionDetails.put("description",jsonNode.get("errorMessage"));
                exceptionDetails.put("isError",true);
            } catch (IOException e) 
            {
            	//response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                statusDetails.put("code",HttpServletResponse.SC_UNAUTHORIZED);
                statusDetails.put("value","Unauthorized");
                exceptionDetails.put("description",authEx.getMessage());
                exceptionDetails.put("isError",true);
            }
        }
        params.put("product",productDetails);
        params.put("status",statusDetails);
        params.put("error",exceptionDetails);
        PrintWriter writer = response.getWriter();

        String jsonParams = null;
        try
        {
            jsonParams = new ObjectMapper().writeValueAsString(params);
            writer.print(jsonParams);
            
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setRealmName("Apirus");
        super.afterPropertiesSet();
    }
}
