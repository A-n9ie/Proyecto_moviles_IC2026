# data/utils/mapper_utils.py

def to_lower_dict(row) -> dict:
    return {k.lower(): v for k, v in dict(row).items()}