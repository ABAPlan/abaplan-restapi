<?php
/**
 * Php proxy representing a Rest Api service for map management
 * (jca)
 */

$request = explode('/', trim($_SERVER['PATH_INFO'],'/'));
$method = $_SERVER['REQUEST_METHOD'];
$action = preg_replace('/[^a-z0-9_]+/i','',array_shift($request));
$key = array_shift($request);
$credentials = require('credentials.php');

$rep = array();

try {
    $db = new PDO('mysql:host='.$credentials['dbhost'].';dbname='.$credentials['dbname'], $credentials['dbuser'], $credentials['dbpass'], array(
        PDO::ATTR_PERSISTENT => true
    ));

    switch ($method) {
    case 'GET':

        switch ($action) {
        case 'maps':
            if ($key) {
                // if the id of the map is provided
                $rep = array('id' => $key, 'name' => 'map with name');

                $preparedStatement = $db->prepare('SELECT * FROM `maps` WHERE uid = :uid');
                $preparedStatement->bindValue('uid', $key, PDO::PARAM_INT);
                $preparedStatement->execute();

                $rep = $preparedStatement->fetch(PDO::FETCH_ASSOC);

            } else {
                // otherwise we provide the full list
                //$rep = array(array('id' => 1, 'name'=>'toto'), array('id' => 2, 'name' => 'titi'));
                $preparedStatement = $db->prepare('SELECT uid, title, city, creationDate FROM `maps`');
                $preparedStatement->execute();

                $rep = $preparedStatement->fetchAll(PDO::FETCH_ASSOC);

            }
            break;
        }

        break;

        case 'POST':
            /*
             * insert a map in the DB and get back the id
             */
            $receivedData = json_decode(file_get_contents('php://input'), true); 
            $validationData = array(
                'height'    => filter_var($receivedData['height'],      FILTER_VALIDATE_INT),
                'width'     => filter_var($receivedData['width'],       FILTER_VALIDATE_INT),
                'city'      => filter_var($receivedData['city'],        FILTER_VALIDATE_BOOLEAN),
                'hash'      => filter_var($receivedData['hash'],        FILTER_SANITIZE_STRING),
                'title'     => filter_var($receivedData['title'],       FILTER_SANITIZE_STRING),
                'creatorId' => filter_var($receivedData['creatorId'],   FILTER_VALIDATE_INT),
                'public'    => filter_var($receivedData['mapIsPublic'], FILTER_VALIDATE_BOOLEAN),
            );

            $validationData['extent'] = json_encode($receivedData['extent']);

            if (empty($receivedData['graphics'])) {
                $validationData['graphics'] = null;
            } else {
                $validationData['graphics'] = json_encode($receivedData['graphics']);
            }
            $rep = $validationData;
            break;

    }
    echo json_encode( $rep, JSON_NUMERIC_CHECK);


} catch (PDOException $e) {
    print "Erreur !: " . $e->getMessage() . "<br/>";
    return;
}

?>
