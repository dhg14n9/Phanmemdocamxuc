import uuid

with open("app/src/main/assets/model-vn/uuid", "w") as f:
    f.write(str(uuid.uuid4()))
