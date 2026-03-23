<?php

namespace Database\Seeders;

use App\Models\Bus;
use App\Models\Driver;
use App\Models\Violation;
use Illuminate\Database\Seeder;

class DatabaseSeeder extends Seeder
{
    /**
     * Seed the application's database.
     */
    public function run(): void
    {
        // Create 10 drivers
        $drivers = [];
        for ($i = 1; $i <= 10; $i++) {
            $drivers[] = Driver::create([
                'name' => 'Driver ' . $i,
            ]);
        }

        // Create 5 recent violations
        $types = ['Speeding', 'Off Route', 'Harsh Braking', 'Late Arrival'];
        foreach ($drivers as $index => $driver) {
            if ($index < 5) {
                Violation::create([
                    'driver_id' => $driver->id,
                    'route' => 'Route ' . rand(1, 20),
                    'type' => $types[array_rand($types)],
                    'created_at' => now()->subMinutes(rand(1, 60)),
                ]);
            }
        }

        // Routes Definition
        $routesData = [
            [
                'route' => '138 Fort-Homagama',
                'lat_min' => 6.8400, 'lat_max' => 6.9330,
                'lng_min' => 79.8400, 'lng_max' => 79.9900
            ],
            [
                'route' => '120 Pettah-Horana',
                'lat_min' => 6.7200, 'lat_max' => 6.9300,
                'lng_min' => 79.8400, 'lng_max' => 79.9500
            ],
            [
                'route' => '100 Pettah-Panadura',
                'lat_min' => 6.7100, 'lat_max' => 6.9300,
                'lng_min' => 79.8400, 'lng_max' => 79.9000
            ],
            [
                'route' => '154 Kiribathgoda-Angulana',
                'lat_min' => 6.8000, 'lat_max' => 6.9700,
                'lng_min' => 79.8600, 'lng_max' => 79.9200
            ],
            [
                'route' => '174 Kottawa-Borella',
                'lat_min' => 6.8400, 'lat_max' => 6.9100,
                'lng_min' => 79.8700, 'lng_max' => 79.9600
            ],
        ];

        // Create 25 buses with random statuses and proper routes
        $statuses = ['Moving', 'Stopped', 'Delayed', 'Offline'];
        for ($i = 1; $i <= 25; $i++) {
            $routeInfo = $routesData[array_rand($routesData)];
            
            // Random float between min and max
            $lat = $routeInfo['lat_min'] + lcg_value() * ($routeInfo['lat_max'] - $routeInfo['lat_min']);
            $lng = $routeInfo['lng_min'] + lcg_value() * ($routeInfo['lng_max'] - $routeInfo['lng_min']);
            
            Bus::create([
                'lat' => $lat,
                'lng' => $lng,
                'status' => $statuses[array_rand($statuses)],
                'route' => $routeInfo['route'],
            ]);
        }
    }
}
