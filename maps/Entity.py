import xml.etree.ElementTree as Et


class Entity:
    fields: list[str]
    type: str
    gold: int
    wood: int
    owner: str
    x: int
    y: int
    remaining_gold: int
    current_hitpoints: int
    width: int
    height: int
    background: list[list[str]]

    def __init__(self, entity_id: int, entity_type: str, **kwargs):
        self.entity_id = entity_id
        self.type = entity_type
        self.fields = ["type"]
        for key, value in kwargs.items():
            self.__setattr__(key, value)
            self.fields.append(key)

    def to_xml(self):
        # print(self.entity_id)
        xml_entity = Et.Element("entity", id=str(self.entity_id))
        for f in self.fields:
            # print(f, self.__getattribute__(f))
            field = Et.Element(f)
            field.text = str(self.__getattribute__(f))
            xml_entity.append(field)
        return xml_entity


class Player(Entity):
    def __init__(self, entity_id: int, gold: int, wood: int, owner: str):
        super().__init__(entity_id, "WPlayer", gold=gold, wood=wood, owner=owner)


class GoldMine(Entity):
    def __init__(self, entity_id: int, x: int, y: int, remaining_gold: int, current_hitpoints: int):
        super().__init__(entity_id, "WGoldMine", x=x, y=y, remaining_gold=remaining_gold,
                         current_hitpoints=current_hitpoints)


class Peasant(Entity):
    def __init__(self, entity_id: int, x: int, y: int, owner: str, current_hitpoints: int):
        super().__init__(entity_id, "WPeasant", x=x, y=y, owner=owner, current_hitpoints=current_hitpoints)


class Map(Entity):
    def __init__(self, entity_id: int, width: int, height: int, padding: str = "w"):
        super().__init__(entity_id, "map", width=width, height=height,
                         background=[[padding] * width for _ in range(height)])

    def __getitem__(self, item):
        return self.background.__getitem__(item)

    def __setitem__(self, key, value):
        return self.background.__setitem__(key, value)

    def to_xml(self):
        print("map", self.entity_id)
        print(self.width, self.height)
        for row in self.background:
            print(*row)
        xml_entity = Et.Element("entity", id=str(self.entity_id))
        for f in self.fields:
            if f != "background":
                # print(f, self.__getattribute__(f))
                field = Et.Element(f)
                field.text = str(self.__getattribute__(f))
                xml_entity.append(field)
            else:
                background = Et.Element("background")
                for r in self.background:
                    row = Et.Element("row")
                    row.text = "".join([point if point in ("w", "t") else "." for point in r])
                    background.append(row)
                xml_entity.append(background)

        return xml_entity
