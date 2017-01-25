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
            $rep = array('id' => 33);
            break;

    }
    echo json_encode( $rep, JSON_NUMERIC_CHECK);


} catch (PDOException $e) {
    print "Erreur !: " . $e->getMessage() . "<br/>";
    return;
}

?>
