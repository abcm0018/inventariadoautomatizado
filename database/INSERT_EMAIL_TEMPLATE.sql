INSERT INTO inventario_db.email_template (template_name, subject, body)
VALUES (
           'new_employee_credentials',
           'Bienvenida - Credenciales de acceso',
           '<!DOCTYPE html>
       <html xmlns:th="http://www.thymeleaf.org">
       <head>
           <meta charset="UTF-8">
           <title th:text="${subject}">Asunto del Email</title>
       </head>
       <body style="font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4;">
           <table width="100%" border="0" cellspacing="0" cellpadding="0">
               <tr>
                   <td align="center">
                       <table width="600" border="0" cellspacing="0" cellpadding="20" style="background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                           <tr>
                               <td style="font-size: 16px; line-height: 1.6; color: #333;">

                                   <h1 style="color: #0056b3;">¡Bienvenido/a al equipo!</h1>
                                   <p>
                                       Hola, <strong th:text="${name} + '' '' + ${surname}">Nombre Apellido</strong>.
                                   </p>
                                   <p>
                                       Te damos la bienvenida al Sistema de Inventario Automatizado. Aquí tienes tus credenciales de acceso para que puedas empezar:
                                   </p>
                                   <ul style="background-color: #f9f9f9; border: 1px solid #eee; padding: 20px; list-style: none;">
                                       <li><strong>Nº de Empleado:</strong> <code th:text="${employeeNumber}">12345</code></li>
                                       <li><strong>Contraseña Temporal:</strong> <code th:text="${password}">temporal123</code></li>
                                   </ul>
                                   <p>
                                       Por favor, cambia tu contraseña la primera vez que inicies sesión.
                                   </p>
                                   <p>¡Mucho éxito en tu primer día!</p>

                                   <hr style="border: 0; border-top: 1px solid #eeeeee; margin: 20px 0;">
                                   <p style="font-size: 12px; color: #888;">
                                       Este es un mensaje automático del Sistema de Inventario Automatizado.
                                       <br>
                                       © 2025 Almacén de Bebidas S.A.
                                   </p>
                               </td>
                           </tr>
                       </table>
                   </td>
               </tr>
           </table>
       </body>
       </html>'
       );

INSERT INTO inventario_db.email_template (template_name, subject, body)
VALUES (
           'password_reset',
           'Restablecimiento de contraseña',
           '<!DOCTYPE html>
       <html xmlns:th="http://www.thymeleaf.org">
       <head>
           <meta charset="UTF-8">
           <title th:text="${subject}">Asunto del Email</title>
       </head>
       <body style="font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4;">
           <table width="100%" border="0" cellspacing="0" cellpadding="0">
               <tr>
                   <td align="center">
                       <table width="600" border="0" cellspacing="0" cellpadding="20" style="background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                           <tr>
                               <td style="font-size: 16px; line-height: 1.6; color: #333;">

                                   <h1 style="color: #0056b3;">Restablece tu contraseña</h1>
                                   <p>
                                       Hola, <strong th:text="${name} + '' '' + ${surname}">Nombre Apellido</strong>.
                                   </p>
                                   <p>
                                       Hemos recibido una solicitud para restablecer la contraseña de tu cuenta. Si no has sido tú, puedes ignorar este correo.
                                   </p>
                                   <p>
                                       Para crear una nueva contraseña, haz clic en el siguiente enlace:
                                   </p>
                                   <p style="text-align: center; margin: 30px 0;">
                                       <a th:href="${resetLink}" style="background-color: #007bff; color: #ffffff; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                                           Restablecer mi contraseña
                                       </a>
                                   </p>
                                   <p style="font-size: 12px; color: #888;">
                                       Si el botón no funciona, copia y pega el siguiente token en la aplicación:
                                       <br>
                                       <code th:text="${resetToken}">TOKEN_AQUI</code>
                                   </p>

                                   <hr style="border: 0; border-top: 1px solid #eeeeee; margin: 20px 0;">
                                   <p style="font-size: 12px; color: #888;">
                                       Este es un mensaje automático del Sistema de Inventario Automatizado.
                                       <br>
                                       © 2025 Almacén de Bebidas S.A.
                                   </p>
                               </td>
                           </tr>
                       </table>
                   </td>
               </tr>
           </table>
       </body>
       </html>'
       );

INSERT INTO inventario_db.email_template (template_name, subject, body)
VALUES (
           'password_changed',
           'Tu contraseña ha sido actualizada',
           '<!DOCTYPE html>
       <html xmlns:th="http://www.thymeleaf.org">
       <head>
           <meta charset="UTF-8">
           <title th:text="${subject}">Asunto del Email</title>
       </head>
       <body style="font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4;">
           <table width="100%" border="0" cellspacing="0" cellpadding="0">
               <tr>
                   <td align="center">
                       <table width="600" border="0" cellspacing="0" cellpadding="20" style="background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                           <tr>
                               <td style="font-size: 16px; line-height: 1.6; color: #333;">

                                   <h1 style="color: #0056b3;">Confirmación de seguridad</h1>
                                   <p>
                                       Hola, <strong th:text="${name} + '' '' + ${surname}">Nombre Apellido</strong>.
                                   </p>
                                   <p>
                                       Te confirmamos que la contraseña de tu cuenta ha sido actualizada correctamente.
                                   </p>
                                   <ul>
                                       <li><strong>Fecha del cambio:</strong> <span th:text="${changeDate}">Fecha</span></li>
                                   </ul>
                                   <p>
                                       Si no has realizado este cambio, por favor, contacta con un administrador inmediatamente.
                                   </p>

                                   <hr style="border: 0; border-top: 1px solid #eeeeee; margin: 20px 0;">
                                   <p style="font-size: 12px; color: #888;">
                                       Este es un mensaje automático del Sistema de Inventario Automatizado.
                                       <br>
                                       © 2025 Almacén de Bebidas S.A.
                                   </p>
                               </td>
                           </tr>
                       </table>
                   </td>
               </tr>
           </table>
       </body>
       </html>'
       );

INSERT INTO inventario_db.email_template (template_name, subject, body)
VALUES (
           'account_locked',
           'Cuenta bloqueada por seguridad',
           '<!DOCTYPE html>
       <html xmlns:th="http://www.thymeleaf.org">
       <head>
           <meta charset="UTF-8">
           <title th:text="${subject}">Asunto del Email</title>
       </head>
       <body style="font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4;">
           <table width="100%" border="0" cellspacing="0" cellpadding="0">
               <tr>
                   <td align="center">
                       <table width="600" border="0" cellspacing="0" cellpadding="20" style="background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                           <tr>
                               <td style="font-size: 16px; line-height: 1.6; color: #333;">

                                   <h1 style="color: #d9534f;">Alerta de Seguridad: Cuenta Bloqueada</h1>
                                   <p>
                                       Hola, <strong th:text="${name} + '' '' + ${surname}">Nombre Apellido</strong>.
                                   </p>
                                   <p>
                                       Tu cuenta ha sido bloqueada temporalmente debido a múltiples intentos fallidos de inicio de sesión.
                                   </p>
                                   <p>
                                       <strong>Instrucciones de desbloqueo:</strong>
                                   </p>
                                   <p th:text="${unlockInstructions}">
                                       Instrucciones aquí...
                                   </p>
                                   <p>
                                       Si no has sido tú quien ha intentado acceder, por favor, informa al departamento de IT.
                                   </p>

                                   <hr style="border: 0; border-top: 1px solid #eeeeee; margin: 20px 0;">
                                   <p style="font-size: 12px; color: #888;">
                                       Este es un mensaje automático del Sistema de Inventario Automatizado.
                                       <br>
                                       © 2025 Almacén de Bebidas S.A.
                                   </p>
                               </td>
                           </tr>
                       </table>
                   </td>
               </tr>
           </table>
       </body>
       </html>'
       );

INSERT INTO inventario_db.email_template (template_name, subject, body)
VALUES (
           'low_stock_alert',
           'Alerta: Stock bajo',
           '<!DOCTYPE html>
       <html xmlns:th="http://www.thymeleaf.org">
       <head>
           <meta charset="UTF-8">
           <title th:text="${subject}">Asunto del Email</title>
       </head>
       <body style="font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4;">
           <table width="100%" border="0" cellspacing="0" cellpadding="0">
               <tr>
                   <td align="center">
                       <table width="600" border="0" cellspacing="0" cellpadding="20" style="background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                           <tr>
                               <td style="font-size: 16px; line-height: 1.6; color: #333;">

                                   <h1 style="color: #f0ad4e;">Alerta: Stock Bajo</h1>
                                   <p>
                                       Se ha detectado un nivel bajo de stock para el siguiente producto:
                                   </p>
                                   <ul style="background-color: #f9f9f9; border: 1px solid #eee; padding: 20px; list-style: none;">
                                       <li><strong>Producto:</strong> <span th:text="${productName}">Nombre Producto</span></li>
                                       <li><strong>EAN:</strong> <span th:text="${productEan}">1234567890123</span></li>
                                       <li><strong>Stock Mínimo:</strong> <span th:text="${minimumStock}">50</span> uds.</li>
                                       <li><strong>Stock Actual:</strong> <strong style="color: #d9534f;" th:text="${currentStock}">45</strong> uds.</li>
                                       <li><strong>Fecha de Alerta:</strong> <span th:text="${alertDate}">Fecha</span></li>
                                   </ul>
                                   <p>
                                       Por favor, gestiona la reposición.
                                   </p>

                                   <hr style="border: 0; border-top: 1px solid #eeeeee; margin: 20px 0;">
                                   <p style="font-size: 12px; color: #888;">
                                       Este es un mensaje automático del Sistema de Inventario Automatizado.
                                       <br>
                                       © 2025 Almacén de Bebidas S.A.
                                   </p>
                               </td>
                           </tr>
                       </table>
                   </td>
               </tr>
           </table>
       </body>
       </html>'
       );

INSERT INTO inventario_db.email_template (template_name, subject, body)
VALUES (
           'critical_stock_alert',
           'ALERTA CRÍTICA: Stock crítico',
           '<!DOCTYPE html>
       <html xmlns:th="http://www.thymeleaf.org">
       <head>
           <meta charset="UTF-8">
           <title th:text="${subject}">Asunto del Email</title>
       </head>
       <body style="font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4;">
           <table width="100%" border="0" cellspacing="0" cellpadding="0">
               <tr>
                   <td align="center">
                       <table width="600" border="0" cellspacing="0" cellpadding="20" style="background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                           <tr>
                               <td style="font-size: 16px; line-height: 1.6; color: #333;">

                                   <h1 style="color: #d9534f;">ALERTA CRÍTICA: Stock Crítico</h1>
                                   <p>
                                       <strong>Atención, <span th:text="${supervisorName}">Nombre Supervisor</span>:</strong>
                                   </p>
                                   <p>
                                       El siguiente producto ha alcanzado un nivel de stock crítico. Se requiere acción inmediata para evitar rotura de stock.
                                   </p>
                                   <ul style="background-color: #fff0f0; border: 1px solid #d9534f; padding: 20px; list-style: none;">
                                       <li><strong>Producto:</strong> <span th:text="${productName}">Nombre Producto</span></li>
                                       <li><strong>EAN:</strong> <span th:text="${productEan}">1234567890123</span></li>
                                       <li><strong>Stock Actual:</strong> <strong style="font-size: 1.2em; color: #d9534f;" th:text="${currentStock}">10</strong> uds.</li>
                                   </ul>
                                   <p>
                                       Por favor, inicia el protocolo de reposición de emergencia.
                                   </p>

                                   <hr style="border: 0; border-top: 1px solid #eeeeee; margin: 20px 0;">
                                   <p style="font-size: 12px; color: #888;">
                                       Este es un mensaje automático del Sistema de Inventario Automatizado.
                                       <br>
                                       © 2025 Almacén de Bebidas S.A.
                                   </p>
                               </td>
                           </tr>
                       </table>
                   </td>
               </tr>
           </table>
       </body>
       </html>'
       );

INSERT INTO inventario_db.email_template (template_name, subject, body)
VALUES (
           'palet_expiry_soon',
           'Aviso: Palets próximos a caducar',
           '<!DOCTYPE html>
       <html xmlns:th="http://www.thymeleaf.org">
       <head>
           <meta charset="UTF-8">
           <title th:text="${subject}">Asunto del Email</title>
       </head>
       <body style="font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4;">
           <table width="100%" border="0" cellspacing="0" cellpadding="0">
               <tr>
                   <td align="center">
                       <table width="600" border="0" cellspacing="0" cellpadding="20" style="background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                           <tr>
                               <td style="font-size: 16px; line-height: 1.6; color: #333;">

                                   <h1 style="color: #f0ad4e;">Aviso: Palets Próximos a Caducar</h1>
                                   <p>
                                       Se ha detectado que palets del siguiente lote están próximos a su fecha de caducidad.
                                   </p>
                                   <ul style="background-color: #f9f9f9; border: 1px solid #eee; padding: 20px; list-style: none;">
                                       <li><strong>Producto:</strong> <span th:text="${productName}">Nombre Producto</span></li>
                                       <li><strong>Lote:</strong> <span th:text="${batchNumber}">LOTE001</span></li>
                                       <li><strong>Fecha de Caducidad:</strong> <span th:text="${expiryDate}">Fecha</span></li>
                                       <li><strong>Días Restantes:</strong> <strong th:text="${daysRemaining}">5</strong> días</li>
                                       <li><strong>Fecha del Reporte:</strong> <span th:text="${reportDate}">Fecha</span></li>
                                   </ul>
                                   <p>
                                       Por favor, asegúrate de que se aplica una rotación FEFO (First Expired, First Out) para este lote.
                                   </p>

                                   <hr style="border: 0; border-top: 1px solid #eeeeee; margin: 20px 0;">
                                   <p style="font-size: 12px; color: #888;">
                                       Este es un mensaje automático del Sistema de Inventario Automatizado.
                                       <br>
                                       © 2025 Almacén de Bebidas S.A.
                                   </p>
                               </td>
                           </tr>
                       </table>
                   </td>
               </tr>
           </table>
       </body>
       </html>'
       );

INSERT INTO inventario_db.email_template (template_name, subject, body)
VALUES (
           'palet_expired',
           'Palets caducados - Acción requerida',
           '<!DOCTYPE html>
       <html xmlns:th="http://www.thymeleaf.org">
       <head>
           <meta charset="UTF-8">
           <title th:text="${subject}">Asunto del Email</title>
       </head>
       <body style="font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4;">
           <table width="100%" border="0" cellspacing="0" cellpadding="0">
               <tr>
                   <td align="center">
                       <table width="600" border="0" cellspacing="0" cellpadding="20" style="background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                           <tr>
                               <td style="font-size: 16px; line-height: 1.6; color: #333;">

                                   <h1 style="color: #d9534f;">Acción Requerida: Palets Caducados</h1>
                                   <p>
                                       <strong>Atención, <span th:text="${supervisorName}">Nombre Supervisor</span>:</strong>
                                   </p>
                                   <p>
                                       Se han detectado palets en el almacén que han superado su fecha de caducidad. Deben ser retirados y gestionados inmediatamente.
                                   </p>
                                   <ul style="background-color: #fff0f0; border: 1px solid #d9534f; padding: 20px; list-style: none;">
                                       <li><strong>Producto:</strong> <span th:text="${productName}">Nombre Producto</span></li>
                                       <li><strong>Lote:</strong> <span th:text="${batchNumber}">LOTE001</span></li>
                                       <li><strong>Fecha de Caducidad:</strong> <span th:text="${expiryDate}" style="font-weight: bold; color: #d9534f;">Fecha</span></li>
                                   </ul>
                                   <p><strong>SSCCs Afectados:</strong></p>
                                   <ul>
                                       <li th:each="sscc : ${ssccList}" th:text="${sscc}">SSCC_001</li>
                                   </ul>
                                   <p>
                                       Por favor, inicia el protocolo de retirada de producto caducado.
                                   </p>

                                   <hr style="border: 0; border-top: 1px solid #eeeeee; margin: 20px 0;">
                                   <p style="font-size: 12px; color: #888;">
                                       Este es un mensaje automático del Sistema de Inventario Automatizado.
                                       <br>
                                       © 2025 Almacén de Bebidas S.A.
                                   </p>
                               </td>
                           </tr>
                       </table>
                   </td>
               </tr>
           </table>
       </body>
       </html>'
       );

INSERT INTO inventario_db.email_template (template_name, subject, body)
VALUES (
           'workshift_assigned',
           'Nuevo turno asignado',
           '<!DOCTYPE html>
       <html xmlns:th="http://www.thymeleaf.org">
       <head>
           <meta charset="UTF-8">
           <title th:text="${subject}">Asunto del Email</title>
       </head>
       <body style="font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4;">
           <table width="100%" border="0" cellspacing="0" cellpadding="0">
               <tr>
                   <td align="center">
                       <table width="600" border="0" cellspacing="0" cellpadding="20" style="background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                           <tr>
                               <td style="font-size: 16px; line-height: 1.6; color: #333;">

                                   <h1 style="color: #0056b3;">Nuevo Turno Asignado</h1>
                                   <p>
                                       Hola, <strong th:text="${employeeName}">Nombre Empleado</strong>.
                                   </p>
                                   <p>
                                       Se te ha asignado un nuevo turno de trabajo:
                                   </p>
                                   <ul style="background-color: #f9f9f9; border: 1px solid #eee; padding: 20px; list-style: none;">
                                       <li><strong>Fecha:</strong> <span th:text="${shiftDate}">Fecha</span></li>
                                       <li><strong>Turno:</strong> <span th:text="${shiftType}">Mañana/Tarde/Noche</span></li>
                                       <li><strong>Horario:</strong> <span th:text="${startTime}">08:00</span> - <span th:text="${endTime}">16:00</span></li>
                                   </ul>
                                   <p>
                                       Por favor, confirma tu asistencia en el portal del empleado.
                                   </p>

                                   <hr style="border: 0; border-top: 1px solid #eeeeee; margin: 20px 0;">
                                   <p style="font-size: 12px; color: #888;">
                                       Este es un mensaje automático del Sistema de Inventario Automatizado.
                                       <br>
                                       © 2025 Almacén de Bebidas S.A.
                                   </p>
                               </td>
                           </tr>
                       </table>
                   </td>
               </tr>
           </table>
       </body>
       </html>'
       );

INSERT INTO inventario_db.email_template (template_name, subject, body)
VALUES (
           'daily_report',
           'Reporte diario de inventario',
           '<!DOCTYPE html>
       <html xmlns:th="http://www.thymeleaf.org">
       <head>
           <meta charset="UTF-8">
           <title th:text="${subject}">Asunto del Email</title>
       </head>
       <body style="font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4;">
           <table width="100%" border="0" cellspacing="0" cellpadding="0">
               <tr>
                   <td align="center">
                       <table width="600" border="0" cellspacing="0" cellpadding="20" style="background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                           <tr>
                               <td style="font-size: 16px; line-height: 1.6; color: #333;">

                                   <h1 style="color: #0056b3;">Reporte Diario de Inventario</h1>
                                   <p>
                                       <strong>A la atención de:</strong> <span th:text="${reportingManager}">Nombre Manager</span>
                                       <br>
                                       <strong>Fecha del reporte:</strong> <span th:text="${reportDate}">Fecha</span>
                                   </p>
                                   <p>
                                       A continuación, se presenta el resumen de estado del inventario al cierre del día:
                                   </p>
                                   <h3 style="border-bottom: 2px solid #007bff; padding-bottom: 5px;">Resumen General</h3>
                                   <ul style="background-color: #f9f9f9; border: 1px solid #eee; padding: 20px; list-style: none;">
                                       <li><strong>Total Palets en Almacén:</strong> <span th:text="${totalPalets}">5000</span></li>
                                       <li><strong>Productos Únicos (SKUs):</strong> <span th:text="${productsCount}">120</span></li>
                                       <li><strong>Productos en Stock Crítico:</strong> <strong style="color: #d9534f;" th:text="${criticalStockCount}">3</strong></li>
                                   </ul>
                                   <p>
                                       Para ver el desglose completo, por favor, accede al dashboard del sistema.
                                   </p>

                                   <hr style="border: 0; border-top: 1px solid #eeeeee; margin: 20px 0;">
                                   <p style="font-size: 12px; color: #888;">
                                       Este es un mensaje automático del Sistema de Inventario Automatizado.
                                       <br>
                                       © 2025 Almacén de Bebidas S.A.
                                   </p>
                               </td>
                           </tr>
                       </table>
                   </td>
               </tr>
           </table>
       </body>
       </html>'
       );