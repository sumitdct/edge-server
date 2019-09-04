package custom.zuul.filters.pre;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.http.HttpServletRequestWrapper;
import com.sumitchouksey.book.vos.RoleVo;
import com.sumitchouksey.book.vos.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import com.netflix.zuul.http.ServletInputStreamWrapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

@Component
public class RequestBodyWrapperFilter extends ZuulFilter
{
    @Value("${identityProviderUrl}")
    private String identityProviderUrl;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return -1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest httpServletRequest = requestContext.getRequest();

        UserVo userVO = null;
        if (httpServletRequest.getHeader("Authorization") != null)
            userVO = getSessionDetails(httpServletRequest.getHeader("Authorization"));

        if (httpServletRequest.getMethod().equalsIgnoreCase("GET")) {
        }
        else
        {
            ObjectNode paramNode = objectMapper.createObjectNode();
            if (userVO != null) {
                Locale locale = LocaleContextHolder.getLocale();
                paramNode.put("userId", userVO.getUserId());
                paramNode.put("clientVo", userVO.getName());

                // Preparing RoleIds
                List<Long> roleIds = new ArrayList<Long>();
                List<String> roleNames = new ArrayList<>();
                List<RoleVo> roleVo = (List<RoleVo>) userVO.getRoles();
                if (roleVo != null) {
                    if (!roleVo.isEmpty()) {
                        for (RoleVo vo : roleVo) {
                            roleIds.add(vo.getRoleId());
                            roleNames.add(vo.getRoleName());
                        }
                    }
                }
                ArrayNode roleIdsNode = paramNode.putArray("roleIds");
                ArrayNode roleNamesNode = paramNode.putArray("userRole");
                roleIds.forEach(e -> {
                    roleIdsNode.add(e.longValue());
                });
                roleNamesNode.forEach(e -> {
                    roleNamesNode.add(e.asText());
                });

            }
            byte[] requestBody = injectAdditionalParameterInRequestBody(requestContext, paramNode);
            if(requestBody!=null)
            {
                requestContext.setRequest(new HttpServletRequestWrapper(requestContext.getRequest()) {
                    @Override
                    public ServletInputStream getInputStream() throws IOException {
                        return new ServletInputStreamWrapper(requestBody);
                    }

                    @Override
                    public int getContentLength() {
                        return requestBody.length;
                    }

                    @Override
                    public long getContentLengthLong() {
                        return requestBody.length;
                    }
                });
            }
            else
                requestContext.setRequest(httpServletRequest);

        }
        return null;
    }

    protected byte[] injectAdditionalParameterInRequestBody(RequestContext requestContext, ObjectNode paramNode) {
        try {
            RequestContext context = RequestContext.getCurrentContext();
            InputStream in = (InputStream) context.get("requestEntity");
            if (in == null) {
                in = requestContext.getRequest().getInputStream();
            }
            String requestBody = StreamUtils.copyToString(in, Charset.forName("UTF-8"));
            ObjectNode objectNode;
            try {
                if (requestBody != null && !requestBody.isEmpty()) {
                    objectNode = (ObjectNode) objectMapper.readTree(requestBody);
                    ObjectNode dataNode = (ObjectNode) objectNode.get("data");
                    Iterator<Map.Entry<String, JsonNode>> fields = paramNode.fields();
                    fields.forEachRemaining(e -> {
                        dataNode.put(e.getKey(), e.getValue());
                    });
                    ObjectNode finalDataNode = objectMapper.createObjectNode();
                    finalDataNode.put("data", dataNode);
                    requestBody = objectMapper.writeValueAsString(finalDataNode);
                    return requestBody.getBytes("UTF-8");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public UserVo getSessionDetails(String authorizationHeader) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authorizationHeader);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity entity = new HttpEntity(headers);
        try {
            ResponseEntity responseEntity = restTemplate.exchange(identityProviderUrl+"/identity/user-vo", HttpMethod.GET, entity, UserVo.class);
            return (UserVo) responseEntity.getBody();
        } catch (Exception e) {
        }
        return null;
    }
}