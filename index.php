<?php
/**
 * Php proxy representing a Rest Api service for map management
 * (jca)
 */

$request = explode('/', trim($_SERVER['PATH_INFO'],'/'));
$method = $_SERVER['REQUEST_METHOD'];
$action = preg_replace('/[^a-z0-9_]+/i','',array_shift($request));
$key = array_shift($request);

$rep = array();

switch ($method) {
case 'GET':

    switch ($action) {
    case 'maps':
        if ($key) {
            // if the id of the map is provided
            $rep = array('id' => $key, 'name' => 'map with name');
        } else {
            // otherwise we provide the full list
            $rep = array(array('id' => 1, 'name'=>'toto'), array('id' => 2, 'name' => 'titi'));
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


?>
