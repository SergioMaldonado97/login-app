#!/usr/bin/env python3
"""
Genera los 3 documentos ODT de documentación del proyecto.
Se ejecuta automáticamente en cada git commit via .git/hooks/post-commit
"""

import subprocess
import os
import re
from datetime import datetime
from odf.opendocument import OpenDocumentText
from odf.style import Style, TextProperties, ParagraphProperties, TableProperties, TableColumnProperties, TableCellProperties
from odf.text import H, P
from odf.table import Table, TableColumn, TableRow, TableCell

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DOCS = os.path.join(ROOT, "docs")
BACKEND = os.path.join(ROOT, "login-backend", "src", "main", "java", "com", "sergio", "login")
FRONTEND = os.path.join(ROOT, "login-frontend", "src", "app")

# ── Helpers ODT ──────────────────────────────────────────────────────────────

def nuevo_doc(titulo):
    doc = OpenDocumentText()

    def estilo(name, family, **props):
        s = Style(name=name, family=family)
        tp, pp = {}, {}
        for k, v in props.items():
            if k.startswith("t_"):
                tp[k[2:]] = v
            else:
                pp[k] = v
        if tp: s.addElement(TextProperties(**tp))
        if pp: s.addElement(ParagraphProperties(**pp))
        doc.styles.addElement(s)

    estilo("DocTitle",  "paragraph", t_fontsize="20pt", t_fontweight="bold", t_color="#1F3864", marginbottom="0.2cm")
    estilo("Subtitulo", "paragraph", t_fontsize="10pt", t_color="#666666",   marginbottom="0.4cm")
    estilo("H1",        "paragraph", t_fontsize="14pt", t_fontweight="bold", t_color="#2E74B5", margintop="0.5cm", marginbottom="0.2cm")
    estilo("H2",        "paragraph", t_fontsize="11pt", t_fontweight="bold", t_color="#404040", margintop="0.3cm", marginbottom="0.1cm")
    estilo("Normal",    "paragraph", t_fontsize="10pt")
    estilo("CodeBlock", "paragraph", t_fontname="Courier New", t_fontsize="9pt", backgroundcolor="#F5F5F5", marginleft="0.4cm", margintop="0.05cm", marginbottom="0.05cm")
    estilo("Badge",     "paragraph", t_fontsize="9pt",  t_color="#155724",   backgroundcolor="#D4EDDA", marginbottom="0.3cm")

    th = Style(name="TH", family="table-cell")
    th.addElement(TableCellProperties(backgroundcolor="#2E74B5", padding="0.12cm", border="0.05cm solid #CCCCCC"))
    doc.styles.addElement(th)

    td = Style(name="TD", family="table-cell")
    td.addElement(TableCellProperties(padding="0.1cm", border="0.05cm solid #CCCCCC"))
    doc.styles.addElement(td)

    td2 = Style(name="TD2", family="table-cell")
    td2.addElement(TableCellProperties(backgroundcolor="#EBF3FB", padding="0.1cm", border="0.05cm solid #CCCCCC"))
    doc.styles.addElement(td2)

    th_txt = Style(name="THText", family="paragraph")
    th_txt.addElement(TextProperties(fontweight="bold", color="#FFFFFF", fontsize="10pt"))
    doc.styles.addElement(th_txt)

    # Título y fecha
    doc.text.addElement(H(outlinelevel=1, stylename="DocTitle", text=titulo))
    doc.text.addElement(P(stylename="Subtitulo",
        text=f"Generado automáticamente — {datetime.now().strftime('%d/%m/%Y %H:%M')}"))

    return doc


def h1(doc, text):
    doc.text.addElement(H(outlinelevel=2, stylename="H1", text=text))

def h2(doc, text):
    doc.text.addElement(H(outlinelevel=3, stylename="H2", text=text))

def p(doc, text=""):
    doc.text.addElement(P(stylename="Normal", text=text))

def code(doc, text):
    doc.text.addElement(P(stylename="CodeBlock", text=text))

def tabla(doc, headers, rows, col_widths=None):
    t = Table()
    for _ in headers:
        t.addElement(TableColumn())
    tr = TableRow()
    for hdr in headers:
        tc = TableCell(stylename="TH")
        tc.addElement(P(stylename="THText", text=hdr))
        tr.addElement(tc)
    t.addElement(tr)
    for i, row in enumerate(rows):
        tr = TableRow()
        st = "TD2" if i % 2 else "TD"
        for cell in row:
            tc = TableCell(stylename=st)
            tc.addElement(P(stylename="Normal", text=str(cell)))
            tr.addElement(tc)
        t.addElement(tr)
    doc.text.addElement(t)
    p(doc)

# ── Datos del proyecto ────────────────────────────────────────────────────────

def git(cmd):
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True, cwd=ROOT)
    return result.stdout.strip()

def leer_clases_java():
    clases = []
    anotaciones = {
        "RestController": "@RestController",
        "Service":        "@Service",
        "Component":      "@Component",
        "Configuration":  "@Configuration",
        "Entity":         "@Entity",
        "Repository":     "Repository",
    }
    if not os.path.exists(BACKEND):
        return clases
    for f in sorted(os.listdir(BACKEND)):
        if not f.endswith(".java"):
            continue
        path = os.path.join(BACKEND, f)
        with open(path) as fp:
            src = fp.read()
        nombre = f.replace(".java", "")
        tipo = "Interface" if "interface " in src else "Class"
        anot = next((v for k, v in anotaciones.items() if f"@{k}" in src), "—")
        endpoints = re.findall(r'@(Get|Post|Put|Delete)Mapping\(["\']([^"\']+)', src)
        schema = re.findall(r'@Table\(.*?name\s*=\s*"([^"]+)".*?\)', src, re.DOTALL)
        clases.append({
            "nombre": nombre,
            "tipo": tipo,
            "anotacion": anot,
            "endpoints": endpoints,
            "schema": schema[0] if schema else None,
            "lineas": src.count("\n"),
        })
    return clases

def leer_servicios_angular():
    servicios = []
    carpetas = ["services", "interceptors", "guards"]
    for carpeta in carpetas:
        path = os.path.join(FRONTEND, carpeta)
        if not os.path.exists(path):
            continue
        for f in sorted(os.listdir(path)):
            if not f.endswith(".ts"):
                continue
            ruta = os.path.join(path, f)
            with open(ruta) as fp:
                src = fp.read()
            metodos = re.findall(r'(?:public\s+)?(\w+)\s*\(', src)
            metodos = [m for m in metodos if m not in ("constructor", "if", "for", "switch", "while")]
            servicios.append({
                "archivo": f"{carpeta}/{f}",
                "tipo": carpeta[:-1].capitalize() if carpeta != "interceptors" else "Interceptor",
                "metodos": metodos[:8],
            })
    return servicios


# ── Documento 1: Stack tecnológico ───────────────────────────────────────────

def generar_stack():
    doc = nuevo_doc("Stack Tecnológico — login-app")

    h1(doc, "Tecnologías del proyecto")
    tabla(doc,
        ["Capa", "Tecnología", "Versión"],
        [
            ["Frontend",      "Angular + TypeScript",         "21 / 5.9"],
            ["Backend",       "Spring Boot + Java",           "3.4.5 / 21"],
            ["Base de datos", "Microsoft SQL Server",         "2022"],
            ["ORM",           "Spring Data JPA + Hibernate",  "6.6"],
            ["Autenticación", "Spring Security + JWT (JJWT)", "0.12.6"],
            ["Encriptación",  "BCrypt",                       "Spring Security"],
            ["Build backend", "Apache Maven",                 "3.x"],
            ["Build frontend","Angular CLI + npm",            "21.x"],
        ]
    )

    h1(doc, "Dependencias backend (pom.xml)")
    pom = os.path.join(ROOT, "login-backend", "pom.xml")
    if os.path.exists(pom):
        with open(pom) as f:
            src = f.read()
        deps = re.findall(r'<artifactId>([^<]+)</artifactId>', src)
        starters = [d for d in deps if d not in ("login-backend", "spring-boot-maven-plugin")]
        tabla(doc, ["Dependencia"], [[d] for d in starters])

    h1(doc, "Dependencias frontend (package.json)")
    pkg = os.path.join(ROOT, "login-frontend", "package.json")
    if os.path.exists(pkg):
        import json
        with open(pkg) as f:
            data = json.load(f)
        rows = [[k, v] for k, v in data.get("dependencies", {}).items()]
        tabla(doc, ["Paquete", "Versión"], rows)

    h1(doc, "Base de datos")
    tabla(doc,
        ["Instancia", "Puerto", "Base de datos", "Schemas"],
        [["localhost", "1433", "logindb", "dbo, BRM_CONFIG"]]
    )
    h2(doc, "Tabla dbo.usuarios")
    tabla(doc,
        ["Columna", "Tipo", "Descripción"],
        [
            ["id",       "BIGINT IDENTITY PK", "Clave primaria autoincremental"],
            ["username", "NVARCHAR UNIQUE",    "Nombre de usuario único"],
            ["password", "NVARCHAR",           "Hash BCrypt"],
            ["nombre",   "NVARCHAR",           "Nombre completo"],
            ["rol",      "NVARCHAR",           "ADMIN | REGULAR"],
        ]
    )
    h2(doc, "Tabla BRM_CONFIG.CFG_CONFIG")
    tabla(doc,
        ["Columna", "Tipo", "Descripción"],
        [
            ["CFG_CONFIG_VAR",   "NVARCHAR PK", "Nombre del parámetro"],
            ["CFG_CONFIG_VALOR", "NVARCHAR",    "Valor del parámetro"],
            ["CFG_CONFIG_GRUPO", "NVARCHAR",    "Agrupación lógica"],
        ]
    )

    doc.save(os.path.join(DOCS, "stack_tecnologico.odt"))
    print("  ✔ stack_tecnologico.odt")


# ── Documento 2: Documentación de clases ─────────────────────────────────────

def generar_clases():
    doc = nuevo_doc("Documentación de Clases — login-app")

    # Backend
    h1(doc, "Backend — Java (Spring Boot)")
    tabla(doc,
        ["Clase", "Tipo", "Anotación", "Líneas"],
        [[c["nombre"], c["tipo"], c["anotacion"], str(c["lineas"])]
         for c in leer_clases_java()]
    )

    for c in leer_clases_java():
        h2(doc, c["nombre"] + ".java")
        p(doc, f"Tipo: {c['tipo']}   |   Anotación: {c['anotacion']}   |   Líneas: {c['lineas']}")
        if c["schema"]:
            p(doc, f"Tabla BD: {c['schema']}")
        if c["endpoints"]:
            p(doc, "Endpoints:")
            for method, path in c["endpoints"]:
                code(doc, f"  @{method}Mapping(\"{path}\")")
        p(doc)

    # API REST
    h1(doc, "API REST")
    tabla(doc,
        ["Método", "Endpoint", "Auth", "Descripción"],
        [
            ["POST",   "/api/auth/login",    "Pública", "Login — devuelve JWT + datos de sesión"],
            ["GET",    "/api/config/sesion", "Pública", "Config del grupo SESION"],
            ["GET",    "/api/usuarios",      "JWT",     "Lista usuarios (sin password)"],
            ["POST",   "/api/usuarios",      "JWT",     "Crea usuario"],
            ["PUT",    "/api/usuarios/{id}", "JWT",     "Actualiza usuario"],
            ["DELETE", "/api/usuarios/{id}", "JWT",     "Elimina usuario"],
        ]
    )

    # Frontend
    h1(doc, "Frontend — Angular (TypeScript)")
    h2(doc, "Rutas")
    tabla(doc,
        ["Ruta", "Componente", "Guards"],
        [
            ["/",          "→ /login",          "—"],
            ["/login",     "LoginComponent",    "—"],
            ["/dashboard", "DashboardComponent","authGuard"],
            ["/usuarios",  "UsuariosComponent", "authGuard + adminGuard"],
        ]
    )
    h2(doc, "Servicios, Guards e Interceptores")
    for s in leer_servicios_angular():
        p(doc, f"{s['archivo']}  [{s['tipo']}]")
        if s["metodos"]:
            code(doc, "  Métodos: " + ", ".join(s["metodos"]))
    p(doc)

    doc.save(os.path.join(DOCS, "documentacion_clases.odt"))
    print("  ✔ documentacion_clases.odt")


# ── Documento 3: Historial de commits ────────────────────────────────────────

def generar_historial():
    doc = nuevo_doc("Historial de Commits — login-app")

    rama = git("git rev-parse --abbrev-ref HEAD")
    total = git("git rev-list --count HEAD")
    ultimo = git("git log -1 --format=%cd --date=format:'%d/%m/%Y %H:%M'")

    h1(doc, "Resumen")
    tabla(doc,
        ["Rama activa", "Total de commits", "Último commit"],
        [[rama, total, ultimo]]
    )

    h1(doc, "Commits detallados")
    log = git('git log --format="%H|%h|%ad|%an|%s" --date=format:"%d/%m/%Y %H:%M"')
    rows = []
    for line in log.splitlines():
        parts = line.split("|", 4)
        if len(parts) == 5:
            _, short, fecha, autor, mensaje = parts
            rows.append([short, fecha, autor, mensaje])
    tabla(doc,
        ["Hash", "Fecha", "Autor", "Mensaje"],
        rows
    )

    h1(doc, "Archivos modificados por commit")
    log2 = git("git log --format='%h %s' --name-only")
    bloques = re.split(r'\n(?=[a-f0-9]{7} )', log2.strip())
    for bloque in bloques[:15]:
        lines = bloque.strip().splitlines()
        if not lines:
            continue
        h2(doc, lines[0])
        archivos = [l.strip() for l in lines[1:] if l.strip()]
        for a in archivos:
            code(doc, f"  {a}")
        p(doc)

    doc.save(os.path.join(DOCS, "historial_commits.odt"))
    print("  ✔ historial_commits.odt")


# ── Main ─────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    print(f"Generando documentación en {DOCS}/")
    generar_stack()
    generar_clases()
    generar_historial()
    print("Documentación actualizada.")
