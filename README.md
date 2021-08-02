

 ______  ______  ______  ______     ______  ______  ______  ______  ______      
/\  == \/\  __ \/\  ___\/\__  _\   /\  == \/\  __ \/\  == \/\  ___\/\  == \     
\ \  _-/\ \  __ \ \___  \/_/\ \/   \ \  _-/\ \  __ \ \  _-/\ \  __\\ \  __<     
 \ \_\   \ \_\ \_\/\_____\ \ \_\    \ \_\   \ \_\ \_\ \_\   \ \_____\ \_\ \_\   
  \/_/    \/_/\/_/\/_____/  \/_/     \/_/    \/_/\/_/\/_/    \/_____/\/_/ /_/   
                                                                                
                            ______  ______  ______                              
                           /\  == \/\  == \/\  __ \                             
                           \ \  _-/\ \  __<\ \ \/\ \                            
                            \ \_\   \ \_\ \_\ \_____\                           
                             \/_/    \/_/ /_/\/_____/                           
                                                                                





FreeGeoIP for Laravel 4
=======================

Laravel 4 Library for calling http://freegeoip.net/ API.

In contrary to all other packages wherein it requires that you have the geoip database in your filesystem, this library calls a free service
So you dont really have to worry about downloading and maintaining geoip data from Maxmind in your own server.

Just install the package, add the config and it is ready to use!


Requirements
============

* Java 8
* Your own exam papers. Papers are not provided.

Installation
============

    composer require buonzz/laravel-4-freegeoip:dev-master

Add the service provider and facade in your config/app.php

Service Provider

    Buonzz\GeoIP\Laravel4\ServiceProviders\GeoIPServiceProvider

Facade

    'GeoIP'            => 'Buonzz\GeoIP\Laravel4\Facades\GeoIP',


Usage
=====

Get country of the visitor

    GeoIP::getCountry();  // returns "United States"

Get country code of the visitor

    GeoIP::getCountryCode();  // returns "US"

Get region of the visitor


Credits
=======

* Matt Elliot