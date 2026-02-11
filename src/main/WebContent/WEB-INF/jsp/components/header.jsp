<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="pt-br">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title><%= (request.getAttribute("pageTitle") != null ? request.getAttribute("pageTitle") : "Autoescola") %></title>

  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">

  <style>
    body { background: #f6f7fb; }
    .table-responsive { max-height: 60vh; }
    .required::after { content: " *"; color: #dc3545; }
    code.kbd { background: #212529; color: #fff; padding: .1rem .35rem; border-radius: .25rem; }
  </style>
</head>
<body>
