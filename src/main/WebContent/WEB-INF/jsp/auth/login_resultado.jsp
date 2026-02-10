<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  String resultado = (String) request.getAttribute("resultado");
  if (resultado == null) resultado = "erro";
%>

<% if ("sucesso".equalsIgnoreCase(resultado)) { %>
  <jsp:forward page="/WEB-INF/jsp/auth/login_sucesso.jsp" />
<% } else { %>
  <jsp:forward page="/WEB-INF/jsp/auth/login_erro.jsp" />
<% } %>
