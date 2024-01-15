from random import random


class Location:
    def __init__(self, x: int, y: int):
        self.x = x
        self.y = y

    def euclidean_distance_to(self, another):
        another: Location
        return ((self.x - another.x) ** 2 + (self.y - another.y) ** 2) ** 0.5

    def path_to(self, another) -> list:  # self is not included
        another: Location
        diff_x = another.x - self.x
        diff_y = another.y - self.y

        if diff_x == 0 or diff_y == 0:
            return self.direct_path_to(another)

        slope = diff_y / diff_x
        way_points = [self]
        for delta_x in (range(1, diff_x) if diff_x > 0 else range(-1, diff_x, -1)):
            x = self.x + delta_x
            y = self.y + slope * delta_x
            way_points.append(Location(x, round(y)))
            # if y % 1 == 0:
            #     way_points.append(Location(x, int(y)))
        way_points.append(another)

        path = []
        turn_point_logic = random() > 0.5
        for i in range(len(way_points) - 1):
            start_point = way_points[i]
            end_point = way_points[i + 1]
            if start_point.x == end_point.x or start_point.y == end_point.y:
                path.extend(start_point.direct_path_to(end_point))
            else:
                path.extend(start_point.right_angle_path_to(end_point, turn_point_logic))
                turn_point_logic = not turn_point_logic
        path.pop(-1)
        return path

    def right_angle_path_to(self, another, turn_point_logic: bool) -> list:
        another: Location
        turn_point = Location(self.x, another.y) if turn_point_logic else Location(another.x, self.y)
        path = self.direct_path_to(turn_point)
        path.extend(turn_point.direct_path_to(another))
        return path

    def direct_path_to(self, another) -> list:
        assert self.euclidean_distance_to(another) > 0

        another: Location
        if self.x == another.x:
            step = 1 if another.y > self.y else -1
            return [Location(self.x, y) for y in range(self.y + step, another.y + step, step)]
        else:

            if self.y != another.y:
                print(self.x, self.y, another.x, another.y)
            step = 1 if another.x > self.x else -1
            return [Location(x, self.y) for x in range(self.x + step, another.x + step, step)]


class Square:
    def __init__(self, x_center: int, y_center: int, width: int):
        self.x_left = round(x_center - width / 2)
        self.y_lower = round(y_center - width / 2)
        self.x_right = self.x_left + width - 1
        self.y_higher = self.y_lower + width - 1
        self.x_center = x_center
        self.y_center = y_center
        self.width = width

    def euclidean_distance_to(self, another):
        if isinstance(another, Location):
            another: Location
            if self.x_left <= another.x <= self.x_right and self.y_lower <= another.y <= self.y_higher:
                return 0
            else:
                x_diff = self.x_left - another.x if self.x_left > another.x else another.x - self.x_right
                y_diff = self.y_lower - another.y if self.y_lower > another.y else another.y - self.y_higher
        else:
            another: Square
            collision_radius = (self.width + another.width) / 2
            x_diff = abs(self.x_center - another.x_center)
            y_diff = abs(self.y_center - another.y_center)
            if x_diff <= collision_radius and y_diff <= collision_radius:
                return 0

        return (x_diff ** 2 + y_diff ** 2) ** 0.5
