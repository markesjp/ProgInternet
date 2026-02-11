<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  String resultado = (String) request.getAttribute("resultado");
  String destino = "/WEB-INF/jsp/upload/upload_erro.jsp";
  if (resultado != null && resultado.equalsIgnoreCase("sucesso")) {
    destino = "/WEB-INF/jsp/upload/upload_sucesso.jsp";
  }
%>
<jsp:forward page="<%= destino %>" />
