<?php

//datos de la conexion
$host = "localhost";
$dbname = "escuela";
$username = "root";
$password = "";

// Establecer credenciales para la autenticación básica
$auth_username = "admin";
$auth_password = "admin123";

// Obtener las credenciales de autenticación del encabezado HTTP
if (
    !isset($_SERVER['PHP_AUTH_USER']) || !isset($_SERVER['PHP_AUTH_PW'])
    || $_SERVER['PHP_AUTH_USER'] != $auth_username || $_SERVER['PHP_AUTH_PW'] != $auth_password
) {
    header('HTTP/1.1 401 Unauthorized');
    header('WWW-Authenticate: Basic realm="Acceso restringido"');
    exit;
}

// Conectar a la base de datos
try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname", $username, $password);
} catch (PDOException $e) {
    die("Error de conexión: " . $e->getMessage());
}

// Establecer el encabezado de respuesta a JSON
header('Content-Type: application/json');

// Comprobar el método HTTP utilizado
$method = $_SERVER['REQUEST_METHOD'];



switch ($method) {
    case 'GET':
        // Obtener un alumno específico o todos los alumnos
        if (isset($_GET['id'])) {
            // Obtener un alumno específico
            $stmt = $pdo->prepare("SELECT * FROM profesores WHERE id = ? ");
            $stmt->execute([$_GET['id']]);
            $alumno = $stmt->fetch(PDO::FETCH_ASSOC);
            echo json_encode($alumno);
        } else {
            // Obtener todos los alumnos
            $stmt = $pdo->query("SELECT * FROM profesores order by id desc");
            $alumnos = $stmt->fetchAll(PDO::FETCH_ASSOC);
            echo json_encode($alumnos);
        }
        break;
    case 'POST':
        // Crear un nuevo profesor
        $data = json_decode(file_get_contents('php://input'), true);
        $stmt = $pdo->prepare("INSERT INTO profesores (nombre, apellido, carnet) VALUES (?, ?, ?)");
        $stmt->execute([$data['nombre'], $data['apellido'], $data['carnet']]);
        $profesor_id = $pdo->lastInsertId();
        $alumno = [
            'id' => $profesor_id,
            'nombre' => $data['nombre'],
            'apellido' => $data['apellido'],
            'carnet' => $data['carnet']
        ];
        echo json_encode($alumno);
        break;
    case 'PUT':
        $data = json_decode(file_get_contents('php://input'), true);
        if ($data['carnet'] == "-987") {
            $stmt = $pdo->prepare("DELETE FROM profesor WHERE id = ?");
            $stmt->execute([$data['id']]);
            echo json_encode(['mensaje' => 'El profesor ha sido eliminado correctamente.']);
        } else {
            // Actualizar un profesor existente              
            $stmt = $pdo->prepare("UPDATE profesores SET nombre = ?, apellido = ?, carnet = ? WHERE id = ?");
            $stmt->execute([$data['nombre'], $data['apellido'], $data['carnet'], $data['id']]);
            $profesor = [
                'id' => $data['id'],
                'nombre' => $data['nombre'],
                'apellido' => $data['apellido'],
                'carnet' => $data['carnet']
            ];
            echo json_encode($profesor);
        }
        break;
    case 'DELETE':
        // Obtener el ID del profesor a eliminar desde la variable de ruta
        $ruta = explode('/', $_SERVER['REQUEST_URI']);
        $id = $ruta[count($ruta) - 1];

        // Eliminar un profesor existente
        if (!empty($id)) {
            $stmt = $pdo->prepare("DELETE FROM profesores WHERE id = ?");
            $stmt->execute([$id]);
            echo json_encode(['mensaje' => 'El profesor ha sido eliminado correctamente.']);
        } else {
            // Error: no se ha proporcionado un ID de alumno para actualizar
            header('HTTP/1.1 400 Bad Request');
            echo json_encode(['error' => 'No se ha proporcionado un ID de profesor para actualizar']);
        }
        break;
    default:
        // Método HTTP no válido
        header('HTTP/1.1 405 Method Not Allowed');
        echo json_encode(['error' => 'Método HTTP no válido']);
        break;
}
//Cerrar la conexión con la base de datos
$pdo = null;
?>