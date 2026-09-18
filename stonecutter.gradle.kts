plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.3"

stonecutter parameters {
    swaps["mod_id"] = "\"${property("mod.id")}\";"
    swaps["mod_version"] = "\"${property("mod.version")}\";"
}