<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  String pageTitle = (String) request.getAttribute("pageTitle");
  if (pageTitle == null || pageTitle.isBlank()) pageTitle = "Autoescola";
%>
<!doctype html>
<html lang="pt-br">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title><%= pageTitle %></title>

  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">

  <style>
    body { background: #f6f7fb; }
    .app-card { border: 0; border-radius: 14px; box-shadow: 0 10px 25px rgba(0,0,0,.06); }
    .table-responsive { max-height: 60vh; }
    .required::after { content: " *"; color: #dc3545; }
    code.kbd { background: #212529; color: #fff; padding: .15rem .35rem; border-radius: .35rem; font-size: .85em; }
  </style>
</head>
<body>
