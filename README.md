# Rest API proxy

_Please, don't put production server db credentials. Ask credentials.php to the admin (jca)_

## How to run
```
php5 -S localhost:8000
```

# Verbs
* GET
    * localhost:8000/maps get all the map
    * localhost:8000/maps/:id get the map with the id :id
* POST
    * localhost:8000/maps 

A post message is a JSON with this validation :
```
{
	"height": [int],
	"width": [int],
	"city": [bool],
	"hash": [str],
	"title": [str],
	"creatorId": [int],
	"mapIsPublic": [bool],
    "extent": [json],
	"graphics": [json],
}
```
