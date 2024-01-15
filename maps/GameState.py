import xml.etree.ElementTree as Et
from Entity import Map, Player, Peasant, GoldMine, Entity
from math import floor, pi, cos, sin
from random import random
from Geometric import Location, Square


class GameState:
    # noinspection PyTypeChecker
    def __init__(self, width: int, height: int, base_padding="w"):
        self.base_padding = base_padding
        self.map = Map(0, width, height, base_padding)
        self.player_1 = Player(1, 2000, 1500, "player1")
        self.player_2 = Player(2, 2000, 1500, "player2")
        self.p1: Location = None
        self.p2: Location = None
        self.peasant_1 = None
        self.peasant_2 = None
        self.goldmine_1 = None
        self.goldmine_2 = None

    @staticmethod
    def random_location(center, radius_min: float, perturbation: float):
        center: Location
        radius = radius_min + perturbation * random()
        radian = random() * 2 * pi
        x = round(center.x + radius * cos(radian))
        y = round(center.y + radius * sin(radian))
        return Location(x, y)

    def can_place(self, square: Square):
        x_in_map = 0 <= square.x_left and square.x_right < self.map.width
        y_in_map = 0 <= square.y_lower and square.y_higher < self.map.height
        if x_in_map and y_in_map:
            for x in range(square.x_left, square.x_right + 1):
                for y in range(square.y_lower, square.y_higher + 1):
                    if self.map[y][x] != self.base_padding:
                        return False
            return True
        else:
            return False

    def place_players(self):
        radius = floor(min(self.map.width, self.map.height) / 2)
        center = Location(round(self.map.width / 2) - 1, round(self.map.height / 2) - 1)
        # self.map[center.y][center.x] = "C"
        radius_min = radius * 0.9
        perturbation = radius * 0.05
        p1 = self.random_location(center, radius_min, perturbation)
        p2 = self.random_location(center, radius_min, perturbation)

        # second player should be far enough from the first one
        while p1.euclidean_distance_to(p2) < 1.7 * radius:
            p2 = self.random_location(center, radius_min, perturbation)

        # save
        self.p1 = p1
        self.p2 = p2
        self.peasant_1 = Peasant(3, p1.x, p1.y, "player1", 30)
        self.peasant_2 = Peasant(4, p2.x, p2.y, "player2", 30)

        # write to map
        self.map[p1.y][p1.x] = "S"
        self.map[p2.y][p2.x] = "E"

        # self.draw_path_to(p1, p2, "p")  # connect players to each other

        return self

    def place_cross_points(self, n_cross_points: int):
        pts = [self.p1, self.p2]
        for _ in range(n_cross_points):
            while True:
                new_p = Location(round(random() * (self.map.width - 1)), round(random() * (self.map.height - 1)))
                can_add = True
                distances = []
                for pt in pts:
                    d = new_p.euclidean_distance_to(pt)
                    distances.append(d)
                    if d < 0.8 * floor(min(self.map.width, self.map.height) / 2):
                        can_add = False
                        break
                if can_add:
                    distances.sort()
                    for pt in pts:
                        if new_p.euclidean_distance_to(pt) <= distances[1]:
                            self.draw_path_to(pt, new_p, padding="X")
                    pts.append(new_p)
                    break
        return self

    def draw_path_to(self, l1: Location, l2: Location, padding: str, overwrite=False):
        for p in l1.path_to(l2):
            if self.map[p.y][p.x] != self.base_padding:
                if overwrite:
                    self.map[p.y][p.x] = padding
            else:
                self.map[p.y][p.x] = padding
        return self

    def add_flat_land(self):
        # fake a base and goldmine for each user,
        # connect them to each other, then expand the boundary
        # width/height for base are 4
        # width/height for barracks and goldmine are 3
        building_width = {"0-base": 4, "1-barracks": 3, "2-goldmine": 3}
        for p in (self.p1, self.p2):
            center = p
            radius_min = 2
            perturbation = 6

            for building_type, width in building_width.items():
                width = width + 3
                building_center = self.random_location(center, radius_min, perturbation)
                building = Square(building_center.x, building_center.y, width)

                while not self.can_place(building):
                    building_center = self.random_location(center, radius_min, perturbation)
                    building = Square(building_center.x, building_center.y, width)

                self.add_to_map(building, building_type[0])
                self.draw_path_to(building_center, p, building_type[0])

                if building_type == "2-goldmine":
                    if p is self.p1:
                        self.goldmine_1 = GoldMine(5, building.x_left, building.y_lower, 50000, 25500)
                    else:
                        self.goldmine_2 = GoldMine(6, building.x_left, building.y_lower, 50000, 25500)

        return self

    def add_to_map(self, square: Square, padding: str):
        for x in range(square.x_left, square.x_right + 1):
            for y in range(square.y_lower, square.y_higher + 1):
                self.map[y][x] = padding
        return self

    def expand_land(self, padding: str = "l", iteration: int = 1):
        delta_neighbors = ((-1, -1), (-1, 1), (1, -1), (1, 1))
        for _ in range(iteration):
            new_map = [row.copy() for row in self.map]
            for x in range(self.map.width):
                for y in range(self.map.height):
                    if self.map[y][x] != self.base_padding:
                        for x_delta, y_delta in delta_neighbors:
                            neighbor_x = x + x_delta
                            neighbor_y = y + y_delta
                            x_in_map = 0 <= neighbor_x < self.map.width
                            y_in_map = 0 <= neighbor_y < self.map.height
                            if x_in_map and y_in_map and self.map[neighbor_y][neighbor_x] == self.base_padding:
                                new_map[neighbor_y][neighbor_x] = padding
            self.map.background = new_map
        return self

    def distribute_resource(self):
        # balance trees in resource radius
        resource_radius = self.p1.euclidean_distance_to(self.p2) / 2
        player1_resources = []
        player2_resources = []
        player1_resources_taken = 0
        player2_resources_taken = 0
        for x in range(self.map.width):
            for y in range(self.map.height):
                point = self.map[y][x]
                resource_location = Location(x, y)
                distance1 = resource_location.euclidean_distance_to(self.p1)
                distance2 = resource_location.euclidean_distance_to(self.p2)
                if distance1 < resource_radius:
                    if point == "t":
                        player1_resources_taken += 1
                    elif point == self.base_padding:
                        player1_resources.append({
                            "location": resource_location,
                            "distance": distance1
                        })
                elif distance2 < resource_radius:
                    if point == "t":
                        player2_resources_taken += 1
                    elif point == self.base_padding:
                        player2_resources.append({
                            "location": resource_location,
                            "distance": distance2
                        })

        min_resources_taken = min(player1_resources_taken, player2_resources_taken)
        player1_resources_can_have = player1_resources_taken + len(player1_resources)
        player2_resources_can_have = player2_resources_taken + len(player2_resources)
        min_resources_can_have = min(player1_resources_can_have, player2_resources_can_have)
        # ↓ min_resources_taken + 0.1 * (min_resources_can_have - min_resources_taken)
        resource_should_have_each = max(
            player1_resources_taken, player2_resources_taken,
            int(0.3 * min_resources_can_have + 0.7 * min_resources_taken)
        )
        amount_player1_need_more = resource_should_have_each - player1_resources_taken
        amount_player2_need_more = resource_should_have_each - player2_resources_taken
        player1_resources.sort(key=lambda r: r["distance"])
        player2_resources.sort(key=lambda r: r["distance"])
        assert amount_player1_need_more >= 0 and amount_player2_need_more >= 0
        player1_resources_need_more = player1_resources[:amount_player1_need_more]
        player2_resources_need_more = player2_resources[:amount_player2_need_more]

        for resources_need_more in (player1_resources_need_more, player2_resources_need_more):
            for resource in resources_need_more:
                resource_location = resource["location"]
                self.map[resource_location.y][resource_location.x] = "t"
        return self

    def to_xml(self, file_path: str = "random.xml"):
        gamestate = Et.Element("gamestate")
        entities: list[Entity] = [self.map, self.player_1, self.player_2, self.peasant_1,
                                  self.peasant_2, self.goldmine_1, self.goldmine_2]
        for entity in entities:
            gamestate.append(entity.to_xml())

        tree = Et.ElementTree(gamestate)
        Et.indent(tree, space="\t", level=0)
        tree.write(file_path, encoding="utf-8")


g = GameState(60, 40).place_players().place_cross_points(6).add_flat_land() \
    .expand_land(iteration=1).expand_land("t", 1).distribute_resource()
g.to_xml()
