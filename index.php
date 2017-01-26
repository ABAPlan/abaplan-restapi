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

            $sql = <<<'END'
                INSERT INTO maps 
                (`uid`, `creatorId`, `public`, `title`, `height`, `width`, `extent`, `hash`, `graphics`, `city`, `creationDate`)
                VALUES
                (NULL, :creatorId, :public, :title, :height, :width, :extent, :hash, :graphics, :city, NOW());
END;

            try {
                $preparedStatement = $db->prepare($sql);

                $preparedStatement->bindValue('creatorId',  $validationData['creatorId'],  PDO::PARAM_INT);
                $preparedStatement->bindValue('height',     $validationData['height'],     PDO::PARAM_INT);
                $preparedStatement->bindValue('public',     $validationData['public'],     PDO::PARAM_BOOL);
                $preparedStatement->bindValue('title',      $validationData['title'],      PDO::PARAM_STR);
                $preparedStatement->bindValue('width',      $validationData['width'],      PDO::PARAM_INT);
                $preparedStatement->bindValue('hash',       $validationData['hash'],       PDO::PARAM_STR);
                $preparedStatement->bindValue('extent',     $validationData['extent'],     PDO::PARAM_STR); // map coordinates
                $preparedStatement->bindValue('city',       $validationData['city'],       PDO::PARAM_BOOL);

                // graphics on the map
                if (is_null($validationData['graphics'])) {
                    $preparedStatement->bindValue('graphics', null, PDO::PARAM_NULL);
                } else {
                    $preparedStatement->bindValue('graphics', $validationData['graphics'], PDO::PARAM_STR);
                }


                $preparedStatement->execute();
                $id= $db->lastInsertId();
                //$rep = array('id' => $test);

            } catch (Exception $e) {
                if ($conf['displayError']) {
                    print "Erreur !: " . $e->getMessage();
                }
                die();
            }
            break;

    }
    echo json_encode( $rep, JSON_NUMERIC_CHECK);


} catch (PDOException $e) {
    print "Erreur !: " . $e->getMessage() . "<br/>";
    return;
}

?>
